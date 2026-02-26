/**
 * 注文履歴ページ
 */

import * as api from '../api.js';
import { formatCurrency, formatDate, getDaysAgo, getToday } from '../utils.js';

/**
 * 注文履歴ページをレンダリング
 * @returns {HTMLElement} ページコンテンツ
 */
export async function renderOrderHistory() {
	const container = document.createElement('div');
	container.className = 'order-history-page';

	const today = getToday();
	const weekAgo = getDaysAgo(7);

	container.innerHTML = `
		<!-- フィルター -->
		<div class="card mb-lg">
			<div class="card-body">
				<div class="filter-row">
					<div class="form-group">
						<label class="form-label" for="start-date">開始日</label>
						<input type="date" id="start-date" class="form-input" value="${weekAgo}" max="${today}">
					</div>
					<div class="form-group">
						<label class="form-label" for="end-date">終了日</label>
						<input type="date" id="end-date" class="form-input" value="${today}" max="${today}">
					</div>
					<div class="form-group">
						<label class="form-label" for="status-filter">状態</label>
						<select id="status-filter" class="form-select">
							<option value="">すべて</option>
							<option value="completed">完了</option>
							<option value="cancelled">キャンセル</option>
						</select>
					</div>
					<div class="form-group">
						<label class="form-label" for="search-input">検索</label>
						<input type="text" id="search-input" class="form-input" placeholder="注文ID、テーブル番号...">
					</div>
					<button class="btn btn-primary" id="apply-filter">
						<span>🔍</span> 検索
					</button>
				</div>
			</div>
		</div>

		<!-- 注文一覧 -->
		<div class="card">
			<div class="card-header flex-between">
				<h3 class="card-title">📋 注文履歴</h3>
				<div class="header-actions">
					<span class="order-count" id="order-count">0件</span>
				</div>
			</div>
			<div class="card-body">
				<div class="table-container">
					<table class="data-table">
						<thead>
							<tr>
								<th>注文ID</th>
								<th>日時</th>
								<th>テーブル</th>
								<th>商品</th>
								<th class="text-right">金額</th>
								<th>状態</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody id="orders-table-body">
							<!-- 動的に生成 -->
						</tbody>
					</table>
				</div>

				<!-- ページネーション -->
				<div class="pagination" id="pagination">
					<!-- 動的に生成 -->
				</div>
			</div>
		</div>

		<!-- 注文詳細モーダル -->
		<div class="modal-overlay hidden" id="order-modal">
			<div class="modal">
				<div class="modal-header">
					<h3 class="modal-title">注文詳細</h3>
					<button class="modal-close" id="modal-close">&times;</button>
				</div>
				<div class="modal-body" id="modal-body">
					<!-- 動的に生成 -->
				</div>
			</div>
		</div>
	`;

	// イベントリスナーを設定
	setupOrderEvents(container);

	// 初期データを読み込み
	await loadOrderData(container, {
		startDate: weekAgo,
		endDate: today,
		status: '',
		search: '',
		page: 1,
	});

	return container;
}

/**
 * 注文データを読み込み
 */
async function loadOrderData(container, filters) {
	try {
		// APIからデータを取得
		const params = {
			startDate: filters.startDate,
			endDate: filters.endDate,
			page: filters.page || 1,
			limit: 10,
		};

		// ステータスフィルターをIDに変換
		if (filters.status === 'completed') {
			params.status = 5;
		} else if (filters.status === 'cancelled') {
			params.status = 9;
		}

		const response = await api.get('/sales/orders', params);

		const orders = (response.orders || []).map((order) => ({
			id: order.orderId,
			tableNumber: order.tableName || '-',
			items: (order.items || []).map((item) => ({
				name: item.menuName,
				price: item.menuPrice,
				quantity: 1,
				options: item.options || [],
			})),
			total: order.total,
			createdAt: new Date(order.createdAt),
			status: order.status || '未設定',
		}));
		const pagination = response.pagination || {
			currentPage: 1,
			totalPages: 1,
			totalItems: orders.length,
		};

		// 検索フィルター（クライアント側）
		let filteredOrders = orders;
		if (filters.search) {
			const searchLower = filters.search.toLowerCase();
			filteredOrders = orders.filter(
				(o) =>
					o.id.toLowerCase().includes(searchLower) || String(o.tableNumber).includes(searchLower),
			);
		}

		updateOrdersTable(container, filteredOrders);
		updatePagination(container, pagination, filters);
	} catch (error) {
		console.error('Failed to load order data:', error);
		updateOrdersTable(container, []);
		updatePagination(container, { currentPage: 1, totalPages: 1, totalItems: 0 }, filters);
	}
}

/**
 * 注文テーブルを更新
 */
function updateOrdersTable(container, orders) {
	const tbody = container.querySelector('#orders-table-body');
	const countEl = container.querySelector('#order-count');

	countEl.textContent = `${orders.length}件`;

	if (orders.length === 0) {
		tbody.innerHTML = `
			<tr>
				<td colspan="7" class="text-center">注文が見つかりません</td>
			</tr>
		`;
		return;
	}

	tbody.innerHTML = orders
		.map(
			(order) => `
		<tr>
			<td><code>${order.id}</code></td>
			<td>${formatDate(order.createdAt, 'datetime')}</td>
			<td>${order.tableNumber}</td>
			<td>
				<span class="item-preview">${order.items[0]?.name || '-'}</span>
				${order.items.length > 1 ? `<span class="item-more">他${order.items.length - 1}品</span>` : ''}
			</td>
			<td class="text-right"><strong>${formatCurrency(order.total)}</strong></td>
			<td><span class="status-badge ${getStatusClass(order.status)}">${getStatusText(order.status)}</span></td>
			<td>
				<button class="btn btn-outline btn-sm" data-action="view" data-order-id="${order.id}">
					詳細
				</button>
			</td>
		</tr>
	`,
		)
		.join('');
}

/**
 * ページネーションを更新
 */
function updatePagination(container, pagination, filters) {
	const paginationEl = container.querySelector('#pagination');
	const { currentPage, totalPages, totalItems } = pagination;

	if (totalPages <= 1) {
		paginationEl.innerHTML = '';
		return;
	}

	const pages = [];
	for (let i = 1; i <= totalPages; i++) {
		if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
			pages.push(i);
		} else if (pages[pages.length - 1] !== '...') {
			pages.push('...');
		}
	}

	paginationEl.innerHTML = `
		<button class="pagination-btn" data-page="${currentPage - 1}" ${currentPage === 1 ? 'disabled' : ''}>
			← 前へ
		</button>
		<div class="pagination-pages">
			${pages
				.map((p) =>
					p === '...'
						? '<span class="pagination-ellipsis">...</span>'
						: `<button class="pagination-btn ${p === currentPage ? 'active' : ''}" data-page="${p}">${p}</button>`,
				)
				.join('')}
		</div>
		<button class="pagination-btn" data-page="${currentPage + 1}" ${currentPage === totalPages ? 'disabled' : ''}>
			次へ →
		</button>
	`;
}

/**
 * 注文詳細モーダルを表示
 */
function showOrderDetail(container, orderId) {
	// 現在表示されているテーブルから注文を取得
	// (APIから個別取得する場合は別途実装)
	const tbody = container.querySelector('#orders-table-body');
	const row = tbody.querySelector(`[data-order-id="${orderId}"]`)?.closest('tr');

	// 既にfetchした注文データを使用するため、modalBodyにデータを設定
	const modal = container.querySelector('#order-modal');
	const modalBody = container.querySelector('#modal-body');

	// シンプルな詳細表示
	modalBody.innerHTML = `
		<div class="order-detail">
			<div class="detail-section">
				<h4>基本情報</h4>
				<dl class="detail-list">
					<dt>注文ID</dt>
					<dd><code>${orderId}</code></dd>
				</dl>
			</div>
			<p class="text-muted">詳細情報は注文一覧から確認できます。</p>
		</div>
	`;

	modal.classList.remove('hidden');
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
 */
function setupOrderEvents(container) {
	const startDateInput = container.querySelector('#start-date');
	const endDateInput = container.querySelector('#end-date');
	const statusSelect = container.querySelector('#status-filter');
	const searchInput = container.querySelector('#search-input');
	const applyBtn = container.querySelector('#apply-filter');
	const modal = container.querySelector('#order-modal');
	const modalClose = container.querySelector('#modal-close');

	// フィルター適用
	applyBtn.addEventListener('click', async () => {
		await loadOrderData(container, {
			startDate: startDateInput.value,
			endDate: endDateInput.value,
			status: statusSelect.value,
			search: searchInput.value,
			page: 1,
		});
	});

	// 詳細ボタンクリック
	container.addEventListener('click', (e) => {
		const viewBtn = e.target.closest('[data-action="view"]');
		if (viewBtn) {
			const orderId = viewBtn.dataset.orderId;
			showOrderDetail(container, orderId);
		}
	});

	// モーダルを閉じる
	modalClose.addEventListener('click', () => {
		modal.classList.add('hidden');
	});

	modal.addEventListener('click', (e) => {
		if (e.target === modal) {
			modal.classList.add('hidden');
		}
	});

	// ページネーションクリック
	container.addEventListener('click', async (e) => {
		const pageBtn = e.target.closest('.pagination-btn[data-page]');
		if (pageBtn && !pageBtn.disabled) {
			const page = parseInt(pageBtn.dataset.page);
			await loadOrderData(container, {
				startDate: startDateInput.value,
				endDate: endDateInput.value,
				status: statusSelect.value,
				search: searchInput.value,
				page,
			});
		}
	});
}
