/**
 * カテゴリ別売上ページ
 */

import * as api from '../api.js';
import { formatCurrency, getFirstDayOfMonth, getToday } from '../utils.js';

/**
 * カテゴリ別売上ページをレンダリング
 * @returns {HTMLElement} ページコンテンツ
 */
export async function renderCategorySales() {
	const container = document.createElement('div');
	container.className = 'category-sales-page';

	const today = getToday();
	const firstDay = getFirstDayOfMonth();

	container.innerHTML = `
		<!-- フィルター -->
		<div class="card mb-lg">
			<div class="card-body">
				<div class="filter-row">
					<div class="form-group">
						<label class="form-label" for="start-date">開始日</label>
						<input type="date" id="start-date" class="form-input" value="${firstDay}" max="${today}">
					</div>
					<div class="form-group">
						<label class="form-label" for="end-date">終了日</label>
						<input type="date" id="end-date" class="form-input" value="${today}" max="${today}">
					</div>
					<button class="btn btn-primary" id="apply-filter">
						<span>🔍</span> 表示
					</button>
				</div>
			</div>
		</div>

		<!-- カテゴリ別サマリーカード -->
		<div class="category-cards" id="category-cards">
			<!-- 動的に生成 -->
		</div>

		<!-- カテゴリ別売上テーブル -->
		<div class="card mt-lg">
			<div class="card-header">
				<h3 class="card-title">📁 カテゴリ別売上詳細</h3>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>カテゴリ</th>
								<th class="text-right">売上金額</th>
								<th class="text-right">販売数</th>
								<th class="text-right">商品数</th>
								<th class="text-right">構成比</th>
							</tr>
						</thead>
						<tbody id="categories-table-body">
							<!-- 動的に生成 -->
						</tbody>
						<tfoot id="categories-table-foot">
							<!-- 動的に生成 -->
						</tfoot>
					</table>
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupCategoryEvents(container);

	// 初期データを読み込み
	await loadCategoryData(container, firstDay, today);

	return container;
}

/**
 * カテゴリデータを読み込み
 */
async function loadCategoryData(container, startDate, endDate) {
	try {
		// APIからデータを取得
		const response = await api.get('/sales/categories', { startDate, endDate });

		const categories = (response.categories || []).map((c) => ({
			name: c.name || '-',
			sales: c.sales || 0,
			quantity: c.quantity || 0,
			productCount: c.productCount || 0,
		}));

		const totalSales = response.totalSales || categories.reduce((sum, c) => sum + c.sales, 0);

		updateCategoryCards(container, categories, totalSales);
		updateCategoriesTable(container, categories, totalSales);
	} catch (error) {
		console.error('Failed to load category data:', error);
		updateCategoryCards(container, [], 0);
		updateCategoriesTable(container, [], 0);
	}
}

/**
 * カテゴリカードを更新
 */
function updateCategoryCards(container, categories, totalSales) {
	const cardsContainer = container.querySelector('#category-cards');

	const colors = ['blue', 'green', 'yellow', 'purple', 'pink', 'orange'];
	const icons = ['🍖', '🥗', '🍝', '🍕', '🍰', '🥤', '🍞', '🍳'];

	cardsContainer.innerHTML = categories
		.map((cat, index) => {
			const percentage = totalSales > 0 ? ((cat.sales / totalSales) * 100).toFixed(1) : '0.0';
			const color = colors[index % colors.length];
			const icon = icons[index % icons.length];

			return `
			<div class="category-card">
				<div class="category-icon ${color}">${icon}</div>
				<div class="category-info">
					<h4 class="category-name">${cat.name}</h4>
					<div class="category-sales">${formatCurrency(cat.sales)}</div>
					<div class="category-meta">
						<span>${cat.quantity}個販売</span>
						<span class="category-percentage">${percentage}%</span>
					</div>
				</div>
			</div>
		`;
		})
		.join('');
}

/**
 * カテゴリテーブルを更新
 */
function updateCategoriesTable(container, categories, totalSales) {
	const tbody = container.querySelector('#categories-table-body');
	const tfoot = container.querySelector('#categories-table-foot');

	tbody.innerHTML = categories
		.map((cat) => {
			const percentage = totalSales > 0 ? ((cat.sales / totalSales) * 100).toFixed(1) : '0.0';
			return `
			<tr>
				<td><strong>${cat.name}</strong></td>
				<td class="text-right">${formatCurrency(cat.sales)}</td>
				<td class="text-right">${cat.quantity}個</td>
				<td class="text-right">${cat.productCount}種類</td>
				<td class="text-right">
					<div class="percentage-bar">
						<div class="percentage-fill" style="width: ${percentage}%"></div>
						<span>${percentage}%</span>
					</div>
				</td>
			</tr>
		`;
		})
		.join('');

	const totalQuantity = categories.reduce((sum, c) => sum + c.quantity, 0);
	const totalProducts = categories.reduce((sum, c) => sum + c.productCount, 0);

	tfoot.innerHTML = `
		<tr>
			<td><strong>合計</strong></td>
			<td class="text-right"><strong>${formatCurrency(totalSales)}</strong></td>
			<td class="text-right"><strong>${totalQuantity}個</strong></td>
			<td class="text-right"><strong>${totalProducts}種類</strong></td>
			<td class="text-right"><strong>100%</strong></td>
		</tr>
	`;
}

/**
 * イベントリスナーを設定
 */
function setupCategoryEvents(container) {
	const startDateInput = container.querySelector('#start-date');
	const endDateInput = container.querySelector('#end-date');
	const applyBtn = container.querySelector('#apply-filter');

	applyBtn.addEventListener('click', async () => {
		await loadCategoryData(container, startDateInput.value, endDateInput.value);
	});
}
