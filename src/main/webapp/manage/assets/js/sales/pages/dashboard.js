/**
 * ダッシュボードページ
 */

import * as api from '../api.js';
import { formatCurrency, formatDate, getDaysAgo, getToday } from '../utils.js';

/**
 * ダッシュボードをレンダリング
 * @returns {HTMLElement} ダッシュボードコンテンツ
 */
export async function renderDashboard() {
	const container = document.createElement('div');
	container.className = 'dashboard-page';

	// APIからデータを取得
	let summaryData;
	try {
		summaryData = await fetchDashboardData();
	} catch (error) {
		console.error('Failed to fetch dashboard data:', error);
		summaryData = getEmptySummaryData();
	}

	container.innerHTML = `
		<!-- 統計カード -->
		<div class="stats-grid">
			<div class="stat-card">
				<div class="stat-icon blue">💰</div>
				<div class="stat-content">
					<div class="stat-label">本日の売上</div>
					<div class="stat-value">${formatCurrency(summaryData.todaySales)}</div>
					<div class="stat-change ${summaryData.todayChange >= 0 ? 'positive' : 'negative'}">
						${summaryData.todayChange >= 0 ? '↑' : '↓'} ${Math.abs(summaryData.todayChange)}% 前日比
					</div>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon green">📦</div>
				<div class="stat-content">
					<div class="stat-label">本日の注文数</div>
					<div class="stat-value">${summaryData.todayOrders}件</div>
					<div class="stat-change ${summaryData.orderChange >= 0 ? 'positive' : 'negative'}">
						${summaryData.orderChange >= 0 ? '↑' : '↓'} ${Math.abs(summaryData.orderChange)}% 前日比
					</div>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon yellow">👥</div>
				<div class="stat-content">
					<div class="stat-label">本日の客数</div>
					<div class="stat-value">${summaryData.todayCustomers}人</div>
					<div class="stat-change ${summaryData.customerChange >= 0 ? 'positive' : 'negative'}">
						${summaryData.customerChange >= 0 ? '↑' : '↓'} ${Math.abs(summaryData.customerChange)}% 前日比
					</div>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon purple">🧾</div>
				<div class="stat-content">
					<div class="stat-label">平均客単価</div>
					<div class="stat-value">${formatCurrency(summaryData.averageOrderValue)}</div>
					<div class="stat-change ${summaryData.avgChange >= 0 ? 'positive' : 'negative'}">
						${summaryData.avgChange >= 0 ? '↑' : '↓'} ${Math.abs(summaryData.avgChange)}% 前日比
					</div>
				</div>
			</div>
		</div>

		<!-- メインコンテンツグリッド -->
		<div class="dashboard-grid">
			<!-- 売上推移グラフ -->
			<div class="card dashboard-chart">
				<div class="card-header flex-between">
					<h3 class="card-title">📈 売上推移（過去7日間）</h3>
					<div class="chart-actions">
						<button class="btn btn-outline btn-sm" data-period="7">7日</button>
						<button class="btn btn-outline btn-sm" data-period="14">14日</button>
						<button class="btn btn-outline btn-sm" data-period="30">30日</button>
					</div>
				</div>
				<div class="card-body">
					<div class="chart-placeholder" id="sales-chart">
						<div class="chart-bars">
							${generateChartBars(summaryData.weeklyData)}
						</div>
						<div class="chart-labels">
							${summaryData.weeklyData.map((d) => `<span>${formatDate(d.date, 'short')}</span>`).join('')}
						</div>
					</div>
				</div>
			</div>

			<!-- 人気商品 -->
			<div class="card dashboard-popular">
				<div class="card-header">
					<h3 class="card-title">🏆 人気商品TOP5（本日）</h3>
				</div>
				<div class="card-body">
					<div class="popular-list">
						${generatePopularItems(summaryData.popularItems)}
					</div>
				</div>
			</div>
		</div>

		<!-- 最近の注文 -->
		<div class="card mt-lg">
			<div class="card-header flex-between">
				<h3 class="card-title">📋 最近の注文</h3>
				<a href="#" class="btn btn-outline btn-sm" data-page="orders">すべて見る →</a>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>注文ID</th>
								<th>テーブル</th>
								<th>商品数</th>
								<th>金額</th>
								<th>時間</th>
								<th>状態</th>
							</tr>
						</thead>
						<tbody>
							${generateRecentOrders(summaryData.recentOrders)}
						</tbody>
					</table>
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupDashboardEvents(container);

	return container;
}

/**
 * チャートバーを生成
 * @param {Array} data - 週間データ
 * @returns {string} HTML文字列
 */
function generateChartBars(data) {
	if (!data || data.length === 0) {
		return '<div class="text-center text-muted">データがありません</div>';
	}
	const maxValue = Math.max(...data.map((d) => d.sales), 1);

	return data
		.map((d) => {
			const height = maxValue > 0 ? (d.sales / maxValue) * 100 : 0;
			return `
			<div class="chart-bar-container">
				<div class="chart-bar" style="height: ${height}%" title="${formatCurrency(d.sales)}">
					<span class="chart-bar-value">${formatCurrency(d.sales, false)}</span>
				</div>
			</div>
		`;
		})
		.join('');
}

/**
 * 人気商品リストを生成
 * @param {Array} items - 人気商品
 * @returns {string} HTML文字列
 */
function generatePopularItems(items) {
	return items
		.map(
			(item, index) => `
		<div class="popular-item">
			<span class="popular-rank">${index + 1}</span>
			<div class="popular-info">
				<span class="popular-name">${item.name}</span>
				<span class="popular-count">${item.count}個販売</span>
			</div>
			<span class="popular-sales">${formatCurrency(item.sales)}</span>
		</div>
	`,
		)
		.join('');
}

/**
 * 最近の注文リストを生成
 * @param {Array} orders - 注文リスト
 * @returns {string} HTML文字列
 */
function generateRecentOrders(orders) {
	if (!orders || orders.length === 0) {
		return `
			<tr>
				<td colspan="6" class="text-center">注文がありません</td>
			</tr>
		`;
	}

	return orders
		.map(
			(order) => `
		<tr>
			<td><code>${order.id}</code></td>
			<td>${order.tableNumber}</td>
			<td>${order.itemCount}品</td>
			<td>${formatCurrency(order.total)}</td>
			<td>${formatDate(order.createdAt, 'time').substring(0, 5)}</td>
			<td><span class="status-badge ${getStatusClass(order.status)}">${getStatusText(order.status)}</span></td>
		</tr>
	`,
		)
		.join('');
}

/**
 * ステータステキストを取得
 * @param {string} status - ステータス
 * @returns {string} ステータステキスト
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
 * ダッシュボードのイベントを設定
 * @param {HTMLElement} container - コンテナ要素
 */
function setupDashboardEvents(container) {
	// 期間切り替えボタン
	const periodButtons = container.querySelectorAll('[data-period]');
	periodButtons.forEach((btn) => {
		btn.addEventListener('click', async (e) => {
			periodButtons.forEach((b) => b.classList.remove('active'));
			btn.classList.add('active');

			const days = parseInt(btn.dataset.period);
			await updateChartPeriod(container, days);
		});
	});
}

/**
 * チャート期間を更新
 * @param {HTMLElement} container - コンテナ要素
 * @param {number} days - 表示日数
 */
async function updateChartPeriod(container, days) {
	try {
		const periodData = await fetchPeriodData(days);

		// チャートを更新
		const chartContainer = container.querySelector('#sales-chart');
		chartContainer.innerHTML = `
			<div class="chart-bars">
				${generateChartBars(periodData)}
			</div>
			<div class="chart-labels">
				${periodData.map((d) => `<span>${formatDate(d.date, 'short')}</span>`).join('')}
			</div>
		`;

		// タイトルを更新
		const chartTitle = container.querySelector('.dashboard-chart .card-title');
		chartTitle.textContent = `📈 売上推移（過去${days}日間）`;
	} catch (error) {
		console.error('Failed to update chart period:', error);
	}
}

/**
 * モックサマリーデータを取得
 * @returns {Object} モックデータ
 */
function getEmptySummaryData() {
	return {
		todaySales: 0,
		todayChange: 0,
		todayOrders: 0,
		orderChange: 0,
		todayCustomers: 0,
		customerChange: 0,
		averageOrderValue: 0,
		avgChange: 0,
		weeklyData: [],
		popularItems: [],
		recentOrders: [],
	};
}

/**
 * ダッシュボードデータを取得
 * @returns {Promise<Object>} ダッシュボードデータ
 */
async function fetchDashboardData() {
	const today = getToday();

	// 並列でAPIを呼び出し
	const [summary, weeklyData, popularItems, recentOrdersRes] = await Promise.all([
		api.get('/sales/summary', { date: today }),
		fetchPeriodData(7),
		api.get('/sales/popular', { date: today, limit: 5 }),
		api.get('/sales/orders', { startDate: today, endDate: today, limit: 5 }),
	]);

	// 最近の注文をマッピング
	const recentOrders = (recentOrdersRes.orders || []).map((order) => ({
		id: order.orderId,
		tableNumber: order.tableName || '-',
		itemCount: order.items ? order.items.length : 0,
		total: order.total,
		createdAt: new Date(order.createdAt),
		status: order.status || '未設定',
	}));

	return {
		todaySales: summary.totalSales || 0,
		todayChange: summary.salesChangePercent || 0,
		todayOrders: summary.orderCount || 0,
		orderChange: summary.orderChangePercent || 0,
		todayCustomers: summary.customerCount || 0,
		customerChange: summary.customerChangePercent || 0,
		averageOrderValue: summary.averagePerCustomer || 0,
		avgChange: summary.avgChangePercent || 0,
		weeklyData: weeklyData,
		popularItems: (popularItems || []).map((p) => ({
			name: p.name,
			count: p.quantity,
			sales: p.sales,
		})),
		recentOrders: recentOrders,
	};
}

/**
 * 指定期間のデータを取得
 * @param {number} days - 取得する日数
 * @returns {Promise<Array>} 売上データ
 */
async function fetchPeriodData(days) {
	const today = new Date();
	const endDate = getToday();
	const startDate = getDaysAgo(days - 1);

	const startDateObj = new Date(startDate);
	const endDateObj = new Date(endDate);

	// 取得する必要がある月をすべて計算
	const monthsToFetch = new Set();
	let currentDate = new Date(startDateObj);

	while (currentDate <= endDateObj) {
		monthsToFetch.add(`${currentDate.getFullYear()}-${currentDate.getMonth() + 1}`);
		currentDate.setMonth(currentDate.getMonth() + 1);
		currentDate.setDate(1);
	}

	// 必要な月のデータをすべて取得
	const monthPromises = Array.from(monthsToFetch).map((monthKey) => {
		const [year, month] = monthKey.split('-').map(Number);
		return api.get('/sales/monthly', { year, month });
	});

	const responses = await Promise.all(monthPromises);

	// すべての月のdailyDataを結合
	const allDailyData = responses.flatMap((response) => response.dailyData || []);

	// 指定期間内のデータをフィルタリングしてソート
	return allDailyData
		.filter((d) => {
			const date = new Date(d.date);
			return date >= startDateObj && date <= endDateObj;
		})
		.map((d) => ({
			date: new Date(d.date),
			sales: d.sales,
		}))
		.sort((a, b) => a.date - b.date);
}
