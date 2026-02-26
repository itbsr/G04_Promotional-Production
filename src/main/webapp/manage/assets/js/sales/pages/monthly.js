/**
 * 月別売上ページ
 */

import * as api from '../api.js';
import { formatCurrency, formatDate } from '../utils.js';

/**
 * 月別売上ページをレンダリング
 * @returns {HTMLElement} ページコンテンツ
 */
export async function renderMonthlySales() {
	const container = document.createElement('div');
	container.className = 'monthly-sales-page';

	const today = new Date();
	const currentYear = today.getFullYear();
	const currentMonth = today.getMonth() + 1;

	container.innerHTML = `
		<!-- フィルター -->
		<div class="card mb-lg">
			<div class="card-body">
				<div class="filter-row">
					<div class="form-group">
						<label class="form-label" for="year-select">年</label>
						<select id="year-select" class="form-select">
							${generateYearOptions(currentYear)}
						</select>
					</div>
					<div class="form-group">
						<label class="form-label" for="month-select">月</label>
						<select id="month-select" class="form-select">
							${generateMonthOptions(currentMonth)}
						</select>
					</div>
					<button class="btn btn-primary" id="apply-filter">
						<span>🔍</span> 表示
					</button>
				</div>
			</div>
		</div>

		<!-- 月間サマリー -->
		<div class="stats-grid" id="monthly-summary">
			<!-- 動的に生成 -->
		</div>

		<!-- 日別売上グラフ -->
		<div class="card mb-lg">
			<div class="card-header">
				<h3 class="card-title">📊 日別売上推移</h3>
			</div>
			<div class="card-body">
				<div class="daily-chart" id="daily-chart">
					<!-- 動的に生成 -->
				</div>
			</div>
		</div>

		<!-- 日別売上テーブル -->
		<div class="card">
			<div class="card-header">
				<h3 class="card-title">📅 日別売上一覧</h3>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>日付</th>
								<th class="text-right">売上</th>
								<th class="text-right">注文数</th>
								<th class="text-right">客数</th>
								<th class="text-right">客単価</th>
							</tr>
						</thead>
						<tbody id="daily-table-body">
							<!-- 動的に生成 -->
						</tbody>
					</table>
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupMonthlyEvents(container);

	// 初期データを読み込み
	await loadMonthlyData(container, currentYear, currentMonth);

	return container;
}

/**
 * 年の選択肢を生成
 */
function generateYearOptions(currentYear) {
	const years = [];
	for (let y = currentYear; y >= currentYear - 2; y--) {
		years.push(`<option value="${y}">${y}年</option>`);
	}
	return years.join('');
}

/**
 * 月の選択肢を生成
 */
function generateMonthOptions(currentMonth) {
	const months = [];
	for (let m = 1; m <= 12; m++) {
		const selected = m === currentMonth ? 'selected' : '';
		months.push(`<option value="${m}" ${selected}>${m}月</option>`);
	}
	return months.join('');
}

/**
 * 月別データを読み込み
 */
async function loadMonthlyData(container, year, month) {
	try {
		// APIからデータを取得
		const response = await api.get('/sales/monthly', { year, month });

		const summary = response.summary || {};
		const bestDay = response.bestDay || { date: null, sales: 0 };
		const dailyData = (response.dailyData || []).map((d) => ({
			date: d.date,
			sales: d.sales || 0,
			orders: d.orders || 0,
			customers: d.customers || 0,
			avgPerCustomer: d.avgPerCustomer || 0,
		}));

		// 集計データを整形
		const daysCount = dailyData.length || 1;
		const formattedSummary = {
			totalSales: summary.totalSales || 0,
			salesChange: summary.salesChangePercent || 0,
			totalOrders: summary.orderCount || 0,
			avgDailySales: Math.floor((summary.totalSales || 0) / daysCount),
			bestDay: {
				date: bestDay.date,
				sales: bestDay.sales || 0,
			},
		};

		updateMonthlySummary(container, formattedSummary);
		updateDailyChart(container, dailyData);
		updateDailyTable(container, dailyData);
	} catch (error) {
		console.error('Failed to load monthly data:', error);
		// エラー時は空データを表示
		const emptySummary = {
			totalSales: 0,
			salesChange: 0,
			totalOrders: 0,
			avgDailySales: 0,
			bestDay: { date: null, sales: 0 },
		};
		updateMonthlySummary(container, emptySummary);
		updateDailyChart(container, []);
		updateDailyTable(container, []);
	}
}

/**
 * 月間サマリーを更新
 */
function updateMonthlySummary(container, summary) {
	const summaryContainer = container.querySelector('#monthly-summary');

	summaryContainer.innerHTML = `
		<div class="stat-card">
			<div class="stat-icon blue">💰</div>
			<div class="stat-content">
				<div class="stat-label">月間売上</div>
				<div class="stat-value">${formatCurrency(summary.totalSales)}</div>
				<div class="stat-change ${summary.salesChange >= 0 ? 'positive' : 'negative'}">
					${summary.salesChange >= 0 ? '↑' : '↓'} ${Math.abs(summary.salesChange)}% 前月比
				</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon green">📦</div>
			<div class="stat-content">
				<div class="stat-label">総注文数</div>
				<div class="stat-value">${summary.totalOrders}件</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon yellow">📈</div>
			<div class="stat-content">
				<div class="stat-label">1日平均売上</div>
				<div class="stat-value">${formatCurrency(summary.avgDailySales)}</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon purple">🏆</div>
			<div class="stat-content">
				<div class="stat-label">最高売上日</div>
				<div class="stat-value">${summary.bestDay.date ? formatDate(summary.bestDay.date, 'monthDay') : '-'}</div>
				<div class="stat-change positive">${formatCurrency(summary.bestDay.sales)}</div>
			</div>
		</div>
	`;
}

/**
 * 日別チャートを更新
 */
function updateDailyChart(container, dailyData) {
	const chartContainer = container.querySelector('#daily-chart');
	const maxValue = Math.max(...dailyData.map((d) => d.sales), 1);

	chartContainer.innerHTML = `
		<div class="monthly-bars">
			${dailyData
				.map(
					(d) => `
				<div class="monthly-bar-wrapper" title="${formatDate(d.date, 'japanese')}: ${formatCurrency(d.sales)}">
					<div class="monthly-bar" style="height: ${(d.sales / maxValue) * 100}%"></div>
					<span class="monthly-label">${new Date(d.date).getDate()}</span>
				</div>
			`,
				)
				.join('')}
		</div>
	`;
}

/**
 * 日別テーブルを更新
 */
function updateDailyTable(container, dailyData) {
	const tbody = container.querySelector('#daily-table-body');

	tbody.innerHTML = dailyData
		.map(
			(d) => `
		<tr>
			<td>${formatDate(d.date, 'japanese')}</td>
			<td class="text-right">${formatCurrency(d.sales)}</td>
			<td class="text-right">${d.orders}件</td>
			<td class="text-right">${d.customers}人</td>
			<td class="text-right">${formatCurrency(d.avgPerCustomer)}</td>
		</tr>
	`,
		)
		.join('');
}

/**
 * イベントリスナーを設定
 */
function setupMonthlyEvents(container) {
	const yearSelect = container.querySelector('#year-select');
	const monthSelect = container.querySelector('#month-select');
	const applyBtn = container.querySelector('#apply-filter');

	applyBtn.addEventListener('click', async () => {
		const year = parseInt(yearSelect.value);
		const month = parseInt(monthSelect.value);
		await loadMonthlyData(container, year, month);
	});
}
