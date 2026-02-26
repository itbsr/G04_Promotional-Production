// ホール管理画面のJavaScript
class HallManager {
	constructor() {
		this.apiBaseUrl = 'api/v1/orders';
		this.orders = new Map(); // orderIdをキーとしたMapに変更
		this.menuCache = new Map(); // メニュー情報のキャッシュ
		this.optionCache = new Map(); // オプション情報のキャッシュ
		this.pollingInterval = null;
		this.isOnline = true;

		this.init();
	}

	init() {
		this.setupElements();
		this.startPolling();
		this.setupErrorHandling();
	}

	setupElements() {
		this.ordersContainer = document.getElementById('orders-container');
		this.loadingElement = document.getElementById('loading');
		this.noOrdersElement = document.getElementById('no-orders');
		this.orderCountElement = document.getElementById('order-count');
		this.connectionStatusElement = document.getElementById('connection-status');
	}

	setupErrorHandling() {
		window.addEventListener('online', () => {
			this.isOnline = true;
			this.updateConnectionStatus(true);
			this.startPolling();
		});

		window.addEventListener('offline', () => {
			this.isOnline = false;
			this.updateConnectionStatus(false);
			this.stopPolling();
		});
	}

	updateConnectionStatus(isConnected) {
		if (isConnected) {
			this.connectionStatusElement.textContent = '接続中';
			this.connectionStatusElement.className = 'status-connected';
		} else {
			this.connectionStatusElement.textContent = '切断';
			this.connectionStatusElement.className = 'status-disconnected';
		}
	}

	async fetchOrders() {
		try {
			// ステータス2（調理済み）の注文を取得
			const response = await fetch(`${this.apiBaseUrl}?status=2`);
			if (!response.ok) {
				throw new Error(`HTTP ${response.status}: ${response.statusText}`);
			}
			const newOrders = await response.json();
			this.updateOrdersDiff(newOrders);
			this.updateConnectionStatus(true);
		} catch (error) {
			console.error('注文の取得に失敗しました:', error);
			this.updateConnectionStatus(false);
		}
	}

	updateOrdersDiff(newOrders) {
		// 新しい注文リストをMapに変換
		const newOrdersMap = new Map();
		newOrders.forEach((order) => {
			newOrdersMap.set(order.orderId, order);
		});

		// 削除された注文を特定して削除
		const currentOrderIds = new Set(this.orders.keys());
		const newOrderIds = new Set(newOrdersMap.keys());

		// 削除すべき注文ID（現在あるが、新しいリストにない）
		const removedOrderIds = new Set([...currentOrderIds].filter((id) => !newOrderIds.has(id)));
		removedOrderIds.forEach((orderId) => {
			this.orders.delete(orderId);
		});

		// 新規追加された注文を特定して追加
		const addedOrderIds = new Set([...newOrderIds].filter((id) => !currentOrderIds.has(id)));
		addedOrderIds.forEach((orderId) => {
			const order = newOrdersMap.get(orderId);
			this.orders.set(orderId, order);
		});

		// 変更された注文を特定して更新（必要に応じて）
		const commonOrderIds = new Set([...currentOrderIds].filter((id) => newOrderIds.has(id)));
		commonOrderIds.forEach((orderId) => {
			const currentOrder = this.orders.get(orderId);
			const newOrder = newOrdersMap.get(orderId);

			// 簡単な比較（実際には深い比較が必要な場合もある）
			if (JSON.stringify(currentOrder) !== JSON.stringify(newOrder)) {
				this.orders.set(orderId, newOrder);
			}
		});

		// テーブルごとに再描画
		this.renderOrdersByTable();
		this.updateDisplayState();
	}

	// テーブルごとにグループ化して注文を描画
	async renderOrdersByTable() {
		this.ordersContainer.innerHTML = '';

		// 注文をテーブル名でグループ化
		const ordersByTable = this.groupOrdersByTable();

		// テーブルごとにカードを作成
		for (const [tableName, orders] of ordersByTable.entries()) {
			const tableCard = await this.createTableCard(tableName, orders);
			this.ordersContainer.appendChild(tableCard);
		}
	}

	// 注文をテーブル名でグループ化
	groupOrdersByTable() {
		const ordersByTable = new Map();

		for (const order of this.orders.values()) {
			const tableName = order.tableName;
			if (!ordersByTable.has(tableName)) {
				ordersByTable.set(tableName, []);
			}
			ordersByTable.get(tableName).push(order);
		}

		return ordersByTable;
	}

	removeOrderFromDisplay(orderId, animate = true) {
		// 指定された注文項目を見つけて削除
		const orderElement = document.querySelector(`[data-orderId="${orderId}"]`);
		if (orderElement) {
			if (animate) {
				// アニメーション付きで削除
				orderElement.style.transform = 'translateX(100%)';
				orderElement.style.opacity = '0';
				orderElement.style.transition = 'all 0.3s ease-out';

				setTimeout(() => {
					// テーブルカード内の他の注文をチェック
					const tableCard = orderElement.closest('.table-card');
					const tableName = tableCard?.dataset.tableName;

					// 注文項目を削除
					orderElement.remove();

					// テーブル内の残り注文数をチェック
					if (tableCard) {
						const remainingOrders = tableCard.querySelectorAll('.order-item');
						if (remainingOrders.length === 0) {
							// テーブルカード全体をアニメーション付きで削除
							this.removeTableCard(tableCard, tableName);
						} else {
							// 注文件数を更新
							this.updateTableCardCount(tableCard, remainingOrders.length);
						}
					}
				}, 300);
			} else {
				// アニメーションなしで即座に削除
				orderElement.remove();
			}
		}
	}

	removeTableCard(tableCard, tableName) {
		tableCard.style.transform = 'scale(0.8)';
		tableCard.style.opacity = '0';
		tableCard.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';

		setTimeout(() => {
			tableCard.remove();
			// 表示状態を更新
			this.updateDisplayState();
		}, 400);
	}

	updateTableCardCount(tableCard, remainingCount) {
		const orderCountElement = tableCard.querySelector('.order-count');
		if (orderCountElement) {
			orderCountElement.textContent = `${remainingCount}件の注文`;
			// カウント更新時の視覚的フィードバック
			orderCountElement.style.transform = 'scale(1.1)';
			orderCountElement.style.transition = 'transform 0.2s ease';
			setTimeout(() => {
				orderCountElement.style.transform = 'scale(1)';
			}, 200);
		}
	}

	async loadOrdersAndDisplay() {
		this.loadingElement.style.display = 'flex';
		this.noOrdersElement.style.display = 'none';

		await this.renderOrdersByTable();
		this.updateDisplayState();
	}

	async createTableCard(tableName, orders) {
		try {
			const tableCard = document.createElement('div');
			tableCard.className = 'table-card';
			tableCard.dataset.tableName = tableName;

			// 最も早い注文時刻を取得
			const earliestOrder = orders.reduce((earliest, current) =>
				new Date(current.orderedAt) < new Date(earliest.orderedAt) ? current : earliest,
			);

			const orderTime = new Date(earliestOrder.orderedAt).toLocaleTimeString('ja-JP', {
				hour: '2-digit',
				minute: '2-digit',
				hour12: false,
			});

			// 各注文のメニューとオプションを取得
			const orderItems = await Promise.all(
				orders.map(async (order) => {
					const menuName = await this.getMenuName(order.menuId);
					const optionNames = await Promise.all(
						order.optionIds.map((optionId) => this.getOptionName(optionId)),
					);
					return { order, menuName, optionNames };
				}),
			);

			tableCard.innerHTML = `
				<div class="table-header">
					<div class="table-name">テーブル ${tableName}</div>
					<div class="order-count">${orders.length}件の注文</div>
					<div class="order-time">${orderTime}</div>
				</div>
				<div class="order-items">
					${orderItems
						.map(
							({ order, menuName, optionNames }) => `
						<div class="order-item" data-orderId="${order.orderId}">
							<div class="item-name">${menuName}</div>
							${
								optionNames.length > 0
									? optionNames
											.map((option) => `<div class="item-options">${option}</div>`)
											.join('')
									: ''
							}
						</div>
					`,
						)
						.join('')}
				</div>
				<div class="complete-button">
					<span>全て配膳完了</span>
				</div>
			`;

			// クリックイベントを追加（テーブルの全注文を配膳完了）
			tableCard.querySelector('.complete-button').addEventListener('click', () => {
				this.completeTableServing(tableName, orders);
			});
			tableCard.querySelectorAll('.order-item').forEach((card) => {
				card.addEventListener('click', (event) => {
					event.stopPropagation();
					const target = event.currentTarget;
					const orderId = target.getAttribute('data-orderId');
					// this.completeTableServing(tableName, orders);
					this.completeServing(orderId);
				});
			});

			return tableCard;
		} catch (error) {
			console.error('テーブルカード作成エラー:', error);
			return this.createErrorTableCard(tableName, orders);
		}
	}

	createErrorTableCard(tableName, orders) {
		const tableCard = document.createElement('div');
		tableCard.className = 'table-card error';
		tableCard.dataset.tableName = tableName;
		tableCard.innerHTML = `
			<div class="table-header">
				<div class="table-name">テーブル ${tableName || '不明'}</div>
				<div class="order-count">${orders.length}件の注文</div>
				<div class="order-time">エラー</div>
			</div>
			<div class="order-items">
				<div class="order-item">
					<div class="item-name">メニュー情報の取得に失敗しました</div>
				</div>
			</div>
			<div class="complete-button">
				<span>配膳完了</span>
			</div>
		`;
		return tableCard;
	}

	async completeTableServing(tableName, orders) {
		try {
			// テーブルの全注文を並行してステータス更新
			const updatePromises = orders.map((order) =>
				fetch(`${this.apiBaseUrl}/${order.orderId}/status`, {
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
					},
					body: JSON.stringify({
						statusId: 3, // STATUS_SERVED
					}),
				}),
			);

			const responses = await Promise.all(updatePromises);
			const allSuccessful = responses.every((response) => response.ok);

			if (allSuccessful) {
				// 成功時は該当テーブルの注文をすべて削除
				orders.forEach((order) => {
					this.orders.delete(order.orderId);
				});

				// 表示を更新
				await this.renderOrdersByTable();
				this.updateDisplayState();

				// 成功フィードバック
				this.showNotification(
					`テーブル ${tableName} の配膳が完了しました（${orders.length}件）`,
					'success',
				);
			} else {
				throw new Error('一部の注文の更新に失敗しました');
			}
		} catch (error) {
			console.error('配膳完了エラー:', error);
			this.showNotification('配膳完了の更新に失敗しました', 'error');
		}
	}

	async completeServing(orderId) {
		console.log(orderId);

		// 注文項目の視覚的フィードバック（処理中表示）
		const orderElement = document.querySelector(`[data-orderId="${orderId}"]`);
		if (orderElement) {
			orderElement.style.pointerEvents = 'none';
			orderElement.style.opacity = '0.6';
			orderElement.style.transform = 'scale(0.95)';
			orderElement.style.transition = 'all 0.2s ease';
		}

		try {
			const response = await fetch(`${this.apiBaseUrl}/${orderId}/status`, {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({
					statusId: 3, // STATUS_SERVED から次のステータスに変更（ここでは完了とする）
				}),
			});

			if (response.ok) {
				// データから注文を削除
				this.orders.delete(orderId);

				// アニメーション付きで画面から削除
				this.removeOrderFromDisplay(orderId, true);

				// 成功フィードバック
				this.showNotification('配膳が完了しました', 'success');
			} else {
				// エラー時は元の状態に戻す
				if (orderElement) {
					orderElement.style.pointerEvents = 'auto';
					orderElement.style.opacity = '1';
					orderElement.style.transform = 'scale(1)';
				}
				throw new Error(`配膳完了の更新に失敗しました: ${response.status}`);
			}
		} catch (error) {
			console.error('配膳完了エラー:', error);
			// エラー時は元の状態に戻す
			if (orderElement) {
				orderElement.style.pointerEvents = 'auto';
				orderElement.style.opacity = '1';
				orderElement.style.transform = 'scale(1)';
			}
			this.showNotification('配膳完了の更新に失敗しました', 'error');
		}
	}

	showNotification(message, type = 'info') {
		// 簡易通知機能
		const notification = document.createElement('div');
		notification.style.cssText = `
			position: fixed;
			top: 20px;
			right: 20px;
			padding: 15px 20px;
			border-radius: 8px;
			color: white;
			font-weight: bold;
			z-index: 1000;
			animation: slideInRight 0.3s ease-out;
			${type === 'success' ? 'background: #27ae60;' : 'background: #e74c3c;'}
		`;
		notification.textContent = message;

		document.body.appendChild(notification);

		setTimeout(() => {
			notification.style.animation = 'slideOutRight 0.3s ease-out';
			setTimeout(() => notification.remove(), 300);
		}, 3000);
	}

	async getMenuName(menuId) {
		if (this.menuCache.has(menuId)) {
			return this.menuCache.get(menuId);
		}

		try {
			const response = await fetch(`api/v1/menus/${menuId}`);
			if (response.ok) {
				const menu = await response.json();
				this.menuCache.set(menuId, menu.name);
				return menu.name;
			}
		} catch (error) {
			console.error('メニュー名取得エラー:', error);
		}

		return `メニュー#${menuId}`;
	}

	async getOptionName(optionId) {
		if (this.optionCache.has(optionId)) {
			return this.optionCache.get(optionId);
		}

		try {
			const response = await fetch(`api/v1/options/${optionId}`);
			if (response.ok) {
				const option = await response.json();
				this.optionCache.set(optionId, option.name);
				return option.name;
			}
		} catch (error) {
			console.error('オプション名取得エラー:', error);
		}

		return `オプション#${optionId}`;
	}

	updateDisplayState() {
		const orderCount = this.orders.size;
		this.orderCountElement.textContent = `(${orderCount})`;

		if (orderCount === 0) {
			this.loadingElement.style.display = 'none';
			this.noOrdersElement.style.display = 'block';
			this.ordersContainer.style.display = 'none';
		} else {
			this.loadingElement.style.display = 'none';
			this.noOrdersElement.style.display = 'none';
			this.ordersContainer.style.display = 'grid';
		}
	}

	startPolling() {
		if (this.pollingInterval) {
			clearInterval(this.pollingInterval);
		}

		this.fetchOrders(); // 最初の取得
		this.pollingInterval = setInterval(() => {
			if (this.isOnline) {
				this.fetchOrders();
			}
		}, 3000); // 3秒間隔でポーリング
	}

	stopPolling() {
		if (this.pollingInterval) {
			clearInterval(this.pollingInterval);
			this.pollingInterval = null;
		}
	}
}

// ページ読み込み完了時に初期化
document.addEventListener('DOMContentLoaded', () => {
	new HallManager();
});

// 通知アニメーション用CSS
const style = document.createElement('style');
style.textContent = `
	@keyframes slideInRight {
		from { transform: translateX(100%); }
		to { transform: translateX(0); }
	}
	@keyframes slideOutRight {
		from { transform: translateX(0); }
		to { transform: translateX(100%); }
	}
`;
document.head.appendChild(style);
