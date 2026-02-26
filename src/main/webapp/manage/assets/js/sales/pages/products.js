/**
 * 商品別売上ページ
 */

import * as api from '../api.js';
import { formatCurrency, getFirstDayOfMonth, getToday } from '../utils.js';

/**
 * 商品別売上ページをレンダリング
 * @returns {HTMLElement} ページコンテンツ
 */
export async function renderProductSales() {
	const container = document.createElement('div');
	container.className = 'product-sales-page';

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
					<div class="form-group">
						<label class="form-label" for="sort-by">並び替え</label>
						<select id="sort-by" class="form-select">
							<option value="sales">売上金額順</option>
							<option value="quantity">販売数量順</option>
							<option value="name">商品名順</option>
						</select>
					</div>
					<button class="btn btn-primary" id="apply-filter">
						<span>🔍</span> 表示
					</button>
				</div>
			</div>
		</div>

		<!-- サマリー -->
		<div class="stats-grid mb-lg" id="product-summary">
			<!-- 動的に生成 -->
		</div>

		<!-- 商品別売上テーブル -->
		<div class="card">
			<div class="card-header flex-between">
				<h3 class="card-title">🍽️ 商品別売上一覧</h3>
				<span class="product-count" id="product-count">0商品</span>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>順位</th>
								<th>商品名</th>
								<th>カテゴリ</th>
								<th class="text-right">単価</th>
								<th class="text-right">販売数</th>
								<th class="text-right">売上金額</th>
								<th class="text-right">構成比</th>
							</tr>
						</thead>
						<tbody id="products-table-body">
							<!-- 動的に生成 -->
						</tbody>
					</table>
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupProductEvents(container);

	// 初期データを読み込み
	await loadProductData(container, firstDay, today, 'sales');

	return container;
}

/**
 * 商品データを読み込み
 */
async function loadProductData(container, startDate, endDate, sortBy) {
	try {
		// APIからデータを取得
		const response = await api.get('/sales/products', { startDate, endDate });

		const summary = response.summary || {};
		let products = (response.products || []).map((p) => ({
			name: p.name,
			category: p.category || '-',
			price: p.price || 0,
			quantity: p.quantity || 0,
			sales: p.sales || 0,
		}));

		// クライアント側でソート
		if (sortBy === 'sales') {
			products.sort((a, b) => b.sales - a.sales);
		} else if (sortBy === 'quantity') {
			products.sort((a, b) => b.quantity - a.quantity);
		} else {
			products.sort((a, b) => a.name.localeCompare(b.name));
		}

		// 商品の実際の売上合計を計算（パーセンテージ計算用）
		const actualTotalSales = products.reduce((sum, p) => sum + p.sales, 0);

		const formattedSummary = {
			totalSales: summary.totalSales || 0,
			totalQuantity: summary.totalQuantity || 0,
			topProduct: summary.topProduct || '-',
			productCount: summary.productCount || products.length,
		};

		updateProductSummary(container, formattedSummary);
		updateProductsTable(container, products, actualTotalSales);
	} catch (error) {
		console.error('Failed to load product data:', error);
		const emptySummary = { totalSales: 0, totalQuantity: 0, topProduct: '-', productCount: 0 };
		updateProductSummary(container, emptySummary);
		updateProductsTable(container, [], 0);
	}
}

/**
 * サマリーを更新
 */
function updateProductSummary(container, summary) {
	const summaryContainer = container.querySelector('#product-summary');

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
				<div class="stat-label">総販売数</div>
				<div class="stat-value">${summary.totalQuantity}個</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon yellow">🏆</div>
			<div class="stat-content">
				<div class="stat-label">売上1位</div>
				<div class="stat-value">${summary.topProduct}</div>
			</div>
		</div>
		<div class="stat-card">
			<div class="stat-icon purple">📊</div>
			<div class="stat-content">
				<div class="stat-label">商品数</div>
				<div class="stat-value">${summary.productCount}種類</div>
			</div>
		</div>
	`;
}

/**
 * 商品テーブルを更新
 */
function updateProductsTable(container, products, totalSales) {
	const tbody = container.querySelector('#products-table-body');
	const countEl = container.querySelector('#product-count');

	countEl.textContent = `${products.length}商品`;

	if (products.length === 0) {
		tbody.innerHTML = `
			<tr>
				<td colspan="7" class="text-center">データがありません</td>
			</tr>
		`;
		return;
	}

	tbody.innerHTML = products
		.map((product, index) => {
			const percentage = totalSales > 0 ? ((product.sales / totalSales) * 100).toFixed(1) : '0.0';
			return `
			<tr>
				<td><span class="rank-badge rank-${index + 1}">${index + 1}</span></td>
				<td><strong>${product.name}</strong></td>
				<td>${product.category}</td>
				<td class="text-right">${formatCurrency(product.price)}</td>
				<td class="text-right">${product.quantity}個</td>
				<td class="text-right"><strong>${formatCurrency(product.sales)}</strong></td>
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
}

/**
 * イベントリスナーを設定
 */
function setupProductEvents(container) {
	const startDateInput = container.querySelector('#start-date');
	const endDateInput = container.querySelector('#end-date');
	const sortSelect = container.querySelector('#sort-by');
	const applyBtn = container.querySelector('#apply-filter');

	applyBtn.addEventListener('click', async () => {
		await loadProductData(container, startDateInput.value, endDateInput.value, sortSelect.value);
	});
}
