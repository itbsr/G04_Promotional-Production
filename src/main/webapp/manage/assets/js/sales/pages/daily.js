/**
 * 日別売上ページ
 */

import * as api from '../api.js';
import { formatCurrency, formatDate, getToday } from '../utils.js';

/**
 * 日別売上ページをレンダリング
 * @returns {HTMLElement} ページコンテンツ
 */
export async function renderDailySales() {
	const container = document.createElement('div');
	container.className = 'daily-sales-page';

	const today = getToday();

	container.innerHTML = `
		<!-- フィルター -->
		<div class="card mb-lg">
			<div class="card-body">
				<div class="filter-row">
					<div class="form-group">
						<label class="form-label" for="date-picker">日付を選択</label>
						<input type="date" id="date-picker" class="form-input" value="${today}" max="${today}">
					</div>
					<button class="btn btn-primary" id="apply-filter">
						<span>🔍</span> 表示
					</button>
				</div>
			</div>
		</div>

		<!-- 売上サマリー -->
		<div class="stats-grid" id="daily-summary">
			<!-- 動的に生成 -->
		</div>

		<!-- 時間帯別売上 -->
		<div class="card mb-lg">
			<div class="card-header">
				<h3 class="card-title">⏰ 時間帯別売上</h3>
			</div>
			<div class="card-body">
				<div class="hourly-chart" id="hourly-chart">
					<!-- 動的に生成 -->
				</div>
			</div>
		</div>

		<!-- 注文一覧 -->
		<div class="card">
			<div class="card-header flex-between">
				<h3 class="card-title">📋 注文一覧</h3>
				<span class="order-count" id="order-count">0件</span>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>時間</th>
								<th>注文ID</th>
								<th>テーブル</th>
								<th>商品数</th>
								<th>金額</th>
								<th>状態</th>
							</tr>
						</thead>
						<tbody id="orders-table-body">
							<!-- 動的に生成 -->
						</tbody>
					</table>
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupDailyEvents(container);

	// 初期データを読み込み
	await loadDailyData(container, today);

	return container;
}

/**
 * 日別データを読み込み
 * @param {HTMLElement} container - コンテナ要素
 * @param {string} date - 日付
 */
async function loadDailyData(container, date) {
	try {
		// APIからデータを取得
		const [dailyResponse, ordersResponse] = await Promise.all([
			api.get('/sales/daily', { date }),
			api.get('/sales/orders', { startDate: date, endDate: date, limit: 100 }),
		]);

		const summary = dailyResponse.summary || {};
		const hourlyData = (dailyResponse.hourlyData || []).map((d) => ({
			hour: d.hour,
			sales: d.sales,
		}));

		const orders = (ordersResponse.orders || []).map((order) => ({
			id: order.orderId,
			tableNumber: order.tableName || '-',
			itemCount: order.items ? order.items.length : 0,
			total: order.total,
			createdAt: new Date(order.createdAt),
			status: order.status || '未設定',
		}));

		updateDailySummary(container, summary);

		// 時間帯別チャートを更新
		updateHourlyChart(container, hourlyData);

		// 注文一覧を更新
		updateOrdersTable(container, orders);
	} catch (error) {
		console.error('Failed to load daily data:', error);
		// エラー時は空データを表示
		updateDailySummary(container, {
			totalSales: 0,
			orderCount: 0,
			customerCount: 0,
			averagePerCustomer: 0,
		});
		updateHourlyChart(container, []);
		updateOrdersTable(container, []);
	}
}

/**
 * 日別サマリーを更新
 * @param {HTMLElement} container - コンテナ要素
 * @param {Object} summary - サマリーデータ
 */
function updateDailySummary(container, summary) {
	const summaryContainer = container.querySelector('#daily-summary');

	summaryContainer.innerHTML = `
		<div class="stat-card">
			<div class="stat-icon blue">💰</div>
			<div class="stat-content">
				<div class="stat-label">総売上</div>
				<div class="stat-value">${formatCurrency(summary.totalSales)}</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon green">📦</div>
			<div class="stat-content">
				<div class="stat-label">注文数</div>
				<div class="stat-value">${summary.orderCount}件</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon yellow">👥</div>
			<div class="stat-content">
				<div class="stat-label">客数</div>
				<div class="stat-value">${summary.customerCount}人</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon purple">🧾</div>
			<div class="stat-content">
				<div class="stat-label">客単価</div>
				<div class="stat-value">${formatCurrency(summary.averagePerCustomer)}</div>
			</div>
		</div>
	`;
}

/**
 * 時間帯別チャートを更新
 * @param {HTMLElement} container - コンテナ要素
 * @param {Array} hourlyData - 時間帯別データ
 */
function updateHourlyChart(container, hourlyData) {
	const chartContainer = container.querySelector('#hourly-chart');
	const maxValue = Math.max(...hourlyData.map((d) => d.sales), 1);

	chartContainer.innerHTML = `
		<div class="hourly-bars">
			${hourlyData
				.map(
					(d) => `
				<div class="hourly-bar-wrapper">
					<div class="hourly-bar" style="height: ${(d.sales / maxValue) * 100}%">
						<span class="hourly-value">${formatCurrency(d.sales, false)}</span>
					</div>
					<span class="hourly-label">${d.hour}時</span>
				</div>
			`,
				)
				.join('')}
		</div>
	`;
}

/**
 * 注文一覧を更新
 * @param {HTMLElement} container - コンテナ要素
 * @param {Array} orders - 注文リスト
 */
function updateOrdersTable(container, orders) {
	const tbody = container.querySelector('#orders-table-body');
	const countEl = container.querySelector('#order-count');

	countEl.textContent = `${orders.length}件`;

	if (orders.length === 0) {
		tbody.innerHTML = `
			<tr>
				<td colspan="6" class="text-center">この日の注文はありません</td>
			</tr>
		`;
		return;
	}

	tbody.innerHTML = orders
		.map(
			(order) => `
		<tr>
			<td>${formatDate(order.createdAt, 'time').substring(0, 5)}</td>
			<td><code>${order.id}</code></td>
			<td>${order.tableNumber}</td>
			<td>${order.itemCount}品</td>
			<td>${formatCurrency(order.total)}</td>
			<td><span class="status-badge ${getStatusClass(order.status)}">${getStatusText(order.status)}</span></td>
		</tr>
	`,
		)
		.join('');
}

/**
 * ステータステキストを取得
 */
function getStatusText(status) {
	// DBから取得した表示名をそのまま返す
	return status || '未設定';
}

/**
 * ステータス表示名からCSSクラス名を生成
 * @param {string} status - ステータス表示名
 * @returns {string} CSSクラス名
 */
function getStatusClass(status) {
	const statusClassMap = {
		調理待ち: 'status-cooking',
		配膳待ち: 'status-ready',
		提供済み: 'status-completed',
	};
	return statusClassMap[status] || 'status-default';
}

/**
 * イベントリスナーを設定
 * @param {HTMLElement} container - コンテナ要素
 */
function setupDailyEvents(container) {
	const datePicker = container.querySelector('#date-picker');
	const applyBtn = container.querySelector('#apply-filter');

	applyBtn.addEventListener('click', async () => {
		const selectedDate = datePicker.value;
		await loadDailyData(container, selectedDate);
	});

	datePicker.addEventListener('change', async () => {
		const selectedDate = datePicker.value;
		await loadDailyData(container, selectedDate);
	});
}
