// キッチン管理画面のJavaScript
class KitchenManager {
	constructor() {
		this.apiBaseUrl = 'api/v1/orders';
		this.orders = new Map(); // orderIdをキーとしたMapに変更
		this.processingOrders = new Set(); // 処理中の注文IDを追跡
		this.processingTimeouts = new Map(); // 処理中のタイムアウトIDを管理
		this.progressIntervals = new Map(); // プログレス更新用インターバルID管理
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
			const response = await fetch(`${this.apiBaseUrl}?status=1`);
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
			this.removeOrderFromDisplay(orderId, false); // アニメーションなしで削除
			this.orders.delete(orderId);
		});

		// 新規追加された注文を特定して追加
		const addedOrderIds = new Set([...newOrderIds].filter((id) => !currentOrderIds.has(id)));
		addedOrderIds.forEach((orderId) => {
			const order = newOrdersMap.get(orderId);
			this.orders.set(orderId, order);
			this.addOrderToDisplay(order);
		});

		// 変更された注文を特定して更新（必要に応じて）
		const commonOrderIds = new Set([...currentOrderIds].filter((id) => newOrderIds.has(id)));
		commonOrderIds.forEach((orderId) => {
			const currentOrder = this.orders.get(orderId);
			const newOrder = newOrdersMap.get(orderId);

			// 注文内容に変更がある場合のみ更新
			if (JSON.stringify(currentOrder) !== JSON.stringify(newOrder)) {
				this.orders.set(orderId, newOrder);
				this.updateOrderInDisplay(newOrder);
			}
		});

		this.updateOrderCount();
		this.updateNoOrdersVisibility();
	}

	updateOrderCount() {
		this.orderCountElement.textContent = `(${this.orders.size})`;
	}

	updateNoOrdersVisibility() {
		this.hideLoading();

		if (this.orders.size === 0) {
			this.showNoOrders();
		} else {
			this.hideNoOrders();
		}
	}

	async addOrderToDisplay(order) {
		this.hideNoOrders();
		const orderCard = await this.createOrderCard(order);
		this.ordersContainer.appendChild(orderCard);
	}

	async updateOrderInDisplay(order) {
		const existingCard = document.querySelector(`[data-order-id="${order.orderId}"]`);
		if (existingCard) {
			const newCard = await this.createOrderCard(order);
			this.ordersContainer.replaceChild(newCard, existingCard);
		}
	}

	async renderOrders() {
		this.hideLoading();

		if (this.orders.size === 0) {
			this.showNoOrders();
			return;
		}

		this.hideNoOrders();
		this.ordersContainer.innerHTML = '';

		// 非同期でカードを作成
		const orderPromises = Array.from(this.orders.values()).map((order) =>
			this.createOrderCard(order),
		);

		try {
			const orderCards = await Promise.all(orderPromises);
			orderCards.forEach((card) => {
				this.ordersContainer.appendChild(card);
			});
		} catch (error) {
			console.error('注文カードの作成に失敗しました:', error);
		}
	}

	async createOrderCard(order) {
		const card = document.createElement('div');
		card.className = 'order-card';
		card.dataset.orderId = order.orderId;

		const isProcessing = this.processingOrders.has(order.orderId);
		if (isProcessing) {
			card.classList.add('processing');
		}

		// 注文時刻をフォーマット
		const orderTime = this.formatOrderTime(order.orderedAt);

		// メニュー名とオプション名を取得
		const menuName = await this.getMenuName(order.menuId);
		const optionNames =
			order.optionIds && order.optionIds.length > 0
				? await this.getOptionNames(order.optionIds)
				: [];

		card.innerHTML = `
            <div class="order-header">
                <div class="order-id">注文ID: ${order.orderId.substring(0, 8)}...</div>
                <div class="order-time">${orderTime}</div>
            </div>
            <div class="order-items">
                <div class="order-item">
                    <div class="item-name">${menuName}</div>
                    ${
											optionNames.length > 0
												? optionNames
														.map((option) => `<div class="item-options">${option}</div>`)
														.join('')
												: ''
										}
                </div>
            </div>
            <div class="order-status">調理待ち</div>
            <div class="tap-instruction">${isProcessing ? 'タップでキャンセル' : '調理完了に変更'}</div>
            <div class="processing-overlay">
                <div class="processing-content">
                    <div class="processing-text">ステータス変更確定まで...</div>
                    <div class="countdown-display">
                        <div class="countdown-number" id="countdown-${order.orderId}">5.0</div>
                        <div class="countdown-label">秒</div>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" id="progress-${order.orderId}"></div>
                    </div>
                    <div class="cancel-instruction">タップでキャンセル</div>
                </div>
            </div>
        `;

		// 処理中でもクリックイベントを設定（キャンセル用）
		card.addEventListener('click', () => this.handleOrderClick(order));

		return card;
	}

	formatOrderTime(timestamp) {
		if (!timestamp) return '時刻不明';

		try {
			const date = new Date(timestamp);
			return date.toLocaleTimeString('ja-JP', {
				hour: '2-digit',
				minute: '2-digit',
				second: '2-digit',
			});
		} catch (error) {
			return '時刻不明';
		}
	}

	async handleOrderClick(order) {
		if (this.processingOrders.has(order.orderId)) {
			// 既に処理中の場合はキャンセル
			this.cancelOrderProcessing(order.orderId);
			return;
		}

		this.processingOrders.add(order.orderId);
		this.updateOrderCardProcessing(order.orderId, true);
		this.updateOrderCardInstruction(order.orderId, true);
		this.startProgressTimer(order.orderId, 5000); // 5秒のプログレスタイマー開始

		try {
			// 5秒間の遅延（調理時間をシミュレート）をキャンセル可能にする
			const cancelled = await this.delayWithCancel(5000, order.orderId);

			// 処理が完了したかキャンセルされたかを確認
			if (!this.processingOrders.has(order.orderId)) {
				// キャンセルされた場合は処理を中止
				return;
			}

			// ステータスを2（調理済み）に更新
			await this.updateOrderStatus(order.orderId, 2);

			// 処理完了後、注文リストから削除
			this.removeOrderFromDisplay(order.orderId, true);
			this.orders.delete(order.orderId); // Mapからも削除
			this.processingTimeouts.delete(order.orderId);
		} catch (error) {
			console.error('注文の更新に失敗しました:', error);
			// エラー時は処理中状態を解除
			this.processingOrders.delete(order.orderId);
			this.processingTimeouts.delete(order.orderId);
			this.updateOrderCardProcessing(order.orderId, false);
			this.updateOrderCardInstruction(order.orderId, false);
			this.stopProgressTimer(order.orderId);
			alert('注文の更新に失敗しました。再試行してください。');
		}
	}

	cancelOrderProcessing(orderId) {
		// タイムアウトをクリア
		if (this.processingTimeouts.has(orderId)) {
			clearTimeout(this.processingTimeouts.get(orderId));
			this.processingTimeouts.delete(orderId);
		}

		// プログレスタイマーを停止
		this.stopProgressTimer(orderId);

		// 処理中状態を解除
		this.processingOrders.delete(orderId);
		this.updateOrderCardProcessing(orderId, false);

		// カードの表示も更新
		this.updateOrderCardInstruction(orderId, false);

		console.log(`注文 ${orderId} の調理がキャンセルされました`);
	}

	delayWithCancel(ms, orderId) {
		return new Promise((resolve) => {
			const timeoutId = setTimeout(() => {
				this.processingTimeouts.delete(orderId);
				resolve(false); // タイムアウト完了（キャンセルされなかった）
			}, ms);

			this.processingTimeouts.set(orderId, timeoutId);
		});
	}

	updateOrderCardInstruction(orderId, isProcessing) {
		const card = document.querySelector(`[data-order-id="${orderId}"]`);
		if (card) {
			const instruction = card.querySelector('.tap-instruction');
			if (instruction) {
				instruction.textContent = isProcessing ? 'タップでキャンセル' : '調理完了に変更';
			}
		}
	}

	startProgressTimer(orderId, totalMs) {
		const startTime = Date.now();
		const countdownElement = document.getElementById(`countdown-${orderId}`);
		const progressElement = document.getElementById(`progress-${orderId}`);

		if (!countdownElement || !progressElement) return;

		const updateProgress = () => {
			const elapsed = Date.now() - startTime;
			const remaining = Math.max(0, totalMs - elapsed);
			const progress = Math.min(100, (elapsed / totalMs) * 100);

			// カウントダウン更新（0.1秒単位）
			const remainingSeconds = Math.max(0, remaining / 1000);
			countdownElement.textContent = remainingSeconds.toFixed(1);
			if (progress < 50) {
				progressElement.style.backgroundColor = '#3498db'; // 青
			} else if (progress < 80) {
				progressElement.style.backgroundColor = '#f39c12'; // オレンジ
			} else {
				progressElement.style.backgroundColor = '#e74c3c'; // 赤
			}

			if (remaining <= 0) {
				this.stopProgressTimer(orderId);
			}
		};

		// 即座に更新
		updateProgress();

		// 100ms間隔で更新
		const intervalId = setInterval(updateProgress, 100);
		this.progressIntervals.set(orderId, intervalId);
	}

	stopProgressTimer(orderId) {
		if (this.progressIntervals.has(orderId)) {
			clearInterval(this.progressIntervals.get(orderId));
			this.progressIntervals.delete(orderId);
		}
	}

	updateOrderCardProcessing(orderId, isProcessing) {
		const card = document.querySelector(`[data-order-id="${orderId}"]`);
		if (card) {
			if (isProcessing) {
				card.classList.add('processing');
			} else {
				card.classList.remove('processing');
			}
		}
	}

	removeOrderFromDisplay(orderId, withAnimation = true) {
		const card = document.querySelector(`[data-order-id="${orderId}"]`);
		if (card) {
			if (withAnimation) {
				card.style.animation = 'fadeOut 0.5s ease';
				setTimeout(() => {
					card.remove();
					this.processingOrders.delete(orderId);
					this.processingTimeouts.delete(orderId);
					this.stopProgressTimer(orderId);
					this.updateOrderCount();
					this.updateNoOrdersVisibility();
				}, 500);
			} else {
				card.remove();
				this.processingOrders.delete(orderId);
				this.processingTimeouts.delete(orderId);
				this.stopProgressTimer(orderId);
			}
		}
	}

	async updateOrderStatus(orderId, statusId) {
		const response = await fetch(`${this.apiBaseUrl}/${orderId}/status`, {
			method: 'PUT',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ statusId: statusId }),
		});

		if (!response.ok) {
			throw new Error(`HTTP ${response.status}: ${response.statusText}`);
		}

		return response.json();
	}

	delay(ms) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	showLoading() {
		this.loadingElement.classList.remove('hidden');
	}

	hideLoading() {
		this.loadingElement.classList.add('hidden');
	}

	showNoOrders() {
		this.noOrdersElement.classList.remove('hidden');
	}

	hideNoOrders() {
		this.noOrdersElement.classList.add('hidden');
	}

	startPolling() {
		// 初回読み込み
		this.fetchOrders();

		// 5秒ごとにポーリング
		this.pollingInterval = setInterval(() => {
			if (this.isOnline) {
				this.fetchOrders();
			}
		}, 5000);
	}

	stopPolling() {
		if (this.pollingInterval) {
			clearInterval(this.pollingInterval);
			this.pollingInterval = null;
		}
	}

	/**
	 * メニューIDから名前を取得（キャッシュ機能付き）
	 */
	async getMenuName(menuId) {
		if (this.menuCache.has(menuId)) {
			return this.menuCache.get(menuId);
		}

		try {
			const response = await fetch(`api/v1/menus/${menuId}`);
			if (!response.ok) {
				throw new Error(`HTTP ${response.status}: ${response.statusText}`);
			}
			const menu = await response.json();
			const menuName = menu.name || `メニューID: ${menuId}`;

			// キャッシュに保存
			this.menuCache.set(menuId, menuName);
			return menuName;
		} catch (error) {
			console.error(`メニュー名の取得に失敗しました (ID: ${menuId}):`, error);
			return `メニューID: ${menuId}`;
		}
	}

	/**
	 * オプションIDの配列から名前の配列を取得（キャッシュ機能付き）
	 */
	async getOptionNames(optionIds) {
		const optionNames = [];

		for (const optionId of optionIds) {
			if (this.optionCache.has(optionId)) {
				optionNames.push(this.optionCache.get(optionId));
			} else {
				try {
					const response = await fetch(`api/v1/options/${optionId}`);
					if (!response.ok) {
						throw new Error(`HTTP ${response.status}: ${response.statusText}`);
					}
					const option = await response.json();
					const optionName = option.name || `オプションID: ${optionId}`;

					// キャッシュに保存
					this.optionCache.set(optionId, optionName);
					optionNames.push(optionName);
				} catch (error) {
					console.error(`オプション名の取得に失敗しました (ID: ${optionId}):`, error);
					optionNames.push(`オプションID: ${optionId}`);
				}
			}
		}

		return optionNames;
	}
}

// DOMが読み込まれたらキッチン管理システムを開始
document.addEventListener('DOMContentLoaded', () => {
	new KitchenManager();
});

// フェードアウトアニメーション用CSS
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeOut {
        from {
            opacity: 1;
            transform: scale(1);
        }
        to {
            opacity: 0;
            transform: scale(0.8);
        }
    }
`;
document.head.appendChild(style);
