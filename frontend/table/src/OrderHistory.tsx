import type {} from 'react';
import { useEffect, useRef, useState } from 'react';
import type {} from './constants';
import { API_BASE_PATH_V1, API_SUFFIX_V1 } from './constants';
import type {} from './EmptyOrderHistory';
import { EmptyOrderHistory } from './EmptyOrderHistory';
import type {} from './ErrorScreen';
import { ErrorScreen } from './ErrorScreen';
import type {} from './LoadingScreen';
import { LoadingSpinner } from './LoadingScreen';
import type { MenuItem, Option, Order } from './models';
import {} from './models';

import './OrderHistory.css';

export interface OrderHistoryProps {
	onClose: () => void;
}
export function OrderHistory({ onClose }: OrderHistoryProps) {
	// 注文履歴 / null=ロード中, undefined=エラー
	const [orders, setOrders] = useState<Order[] | undefined | null>(null);
	const [isScrollable, setIsScrollable] = useState(false);
	const containerRef = useRef<HTMLDivElement>(null);

	// 会計処理の状態管理
	const [checkoutState, setCheckoutState] = useState<
		null | 'confirm' | 'processing' | 'success' | 'error' | 'unserved-items'
	>(null);

	useEffect(() => {
		fetchOrders(setOrders);
	}, []);

	// オーダーリスト部分のスクロール可能性をチェック
	useEffect(() => {
		if (orders && containerRef.current) {
			const container = containerRef.current;
			const isContentOverflowing = container.scrollHeight > container.clientHeight;
			setIsScrollable(isContentOverflowing);
		}
	}, [orders]);

	const sTotalPrice: string =
		orders
			?.reduce(
				(sum, order) =>
					sum + order.menuItem.price + order.options.reduce((optSum, opt) => optSum + opt.price, 0),
				0,
			)
			.toString() || '----';

	const handleCheckoutClick = () => {
		setCheckoutState('confirm');
	};

	const handleConfirmCheckout = async () => {
		setCheckoutState('processing');

		try {
			const response = await fetch(`${API_BASE_PATH_V1}/customers/checkout${API_SUFFIX_V1}`, {
				method: 'PUT',
			});

			if (response.status === 409) {
				// 未配膳商品があることを通知して確認モーダルを表示
				setCheckoutState('unserved-items');
				return;
			}

			if (!response.ok) {
				throw new Error('Checkout failed');
			}

			// 成功時の処理
			setCheckoutState('success');

			// 30秒後に画面を再読み込み
			setTimeout(() => {
				location.replace(location.href);
			}, 30000);
		} catch (error) {
			console.error('Checkout error:', error);
			setCheckoutState('error');
		}
	};

	const handleCancelCheckout = () => {
		setCheckoutState(null);
	};

	const handleOnError = () => {
		setCheckoutState(null);
	};

	const handleProceedWithUnserved = async () => {
		setCheckoutState('processing');

		try {
			const url = API_SUFFIX_V1.includes('?')
				? `${API_BASE_PATH_V1}/customers/checkout${API_SUFFIX_V1}&force=true`
				: `${API_BASE_PATH_V1}/customers/checkout${API_SUFFIX_V1}?force=true`;

			const response = await fetch(url, {
				method: 'PUT',
			});

			if (!response.ok) {
				throw new Error('Forced checkout failed');
			}

			// 成功時の処理
			setCheckoutState('success');

			// 30秒後に画面を再読み込み
			setTimeout(() => {
				location.replace(location.href);
			}, 30000);
		} catch (error) {
			console.error('Forced checkout error:', error);
			setCheckoutState('error');
		}
	};

	const handleCancelUnserved = () => {
		setCheckoutState(null);
	};

	const renderCheckoutModal = () => {
		switch (checkoutState) {
			case 'confirm':
				return (
					<CheckoutConfirmModal onConfirm={handleConfirmCheckout} onCancel={handleCancelCheckout} />
				);
			case 'processing':
				return <CheckoutProcessingMessage />;
			case 'unserved-items':
				return (
					<UnservedItemsConfirmModal
						onProceed={handleProceedWithUnserved}
						onCancel={handleCancelUnserved}
					/>
				);
			case 'success':
				return <CheckoutSuccessMessage />;
			case 'error':
				return <CheckoutErrorMessage onClose={handleOnError} />;
			default:
				return null;
		}
	};

	return (
		<>
			<div className='order-history-container'>
				<div className='order-history-caption'>注文履歴</div>
				<div
					ref={containerRef}
					// スクロール可能な場合はスクロールバーを表示し、そうでない場合は最後のアイテムにボーダーを表示
					className={`order-items-container ${!isScrollable ? 'show-last-border' : ''}`}
				>
					{orders === null && <LoadingSpinner message='注文履歴を読み込み中' />}
					{orders === undefined && <ErrorScreen message='注文履歴の取得に失敗しました。' />}
					{orders && orders.length === 0 && <EmptyOrderHistory />}
					{orders && orders.length > 0 && orders.map((order) => <OrderItem key={order.orderId} order={order} />)}
				</div>
				<OrderActionButtons
					totalPrice={sTotalPrice}
					onClose={onClose}
					onCheckout={handleCheckoutClick}
					isProcessing={checkoutState === 'processing'}
				/>
			</div>

			{renderCheckoutModal()}
		</>
	);
}

function OrderItem({ order }: { order: Order }) {
	const totalPrice = order.menuItem.price + order.options.reduce((sum, opt) => sum + opt.price, 0);

	return (
		<div className='order-history-item'>
			<div className='order-item-image'>
				<img src={order.menuItem.image} alt={order.menuItem.name} />
			</div>
			<div className='order-item-details'>
				<div className='order-item-left'>
					<div className='order-item-name'>{order.menuItem.name}</div>
					{order.options.length > 0 && (
						<div className='order-item-options'>
							<p className='order-options-label'>オプション:</p>
							{order.options.map((option) => (
								<span key={option.id} className='order-option-item'>
									{option.name} (+{option.price}円)
								</span>
							))}
						</div>
					)}
				</div>
				<div className='order-item-right'>
					<span className={`order-item-status ${order.statusId === 3 ? 'completed' : ''}`}>
						{order.statusName}
					</span>
					<div className='order-item-price'>{totalPrice.toLocaleString()}円</div>
				</div>
			</div>
		</div>
	);
}

interface OrderActionButtonsProps {
	totalPrice: string;
	onClose: () => void;
	onCheckout: () => void;
	isProcessing: boolean;
}
function OrderActionButtons({
	totalPrice,
	onClose,
	onCheckout,
	isProcessing,
}: OrderActionButtonsProps) {
	return (
		<div className='order-action-buttons-container'>
			<div className='order-action-side order-action-left'>
				<button
					className='orders-action-button orders-back-button'
					onClick={onClose}
					disabled={isProcessing}
				>
					メニューへ戻る
				</button>
			</div>
			<div className='order-action-side order-action-right'>
				<div className='order-total-price'>合計金額: {totalPrice}円</div>
				<button
					className='orders-action-button orders-checkout-button'
					onClick={onCheckout}
					disabled={isProcessing}
				>
					{isProcessing ? '処理中...' : '会計へ進む'}
				</button>
			</div>
		</div>
	);
}

interface CheckoutConfirmModalProps {
	onConfirm: () => void;
	onCancel: () => void;
}
function CheckoutConfirmModal({ onConfirm, onCancel }: CheckoutConfirmModalProps) {
	return (
		<div className='checkout-modal-overlay'>
			<div className='checkout-modal'>
				<div className='checkout-modal-message'>お会計を確定してよろしいですか?</div>
				<div className='checkout-modal-buttons'>
					<button className='checkout-modal-button cancel' onClick={onCancel}>
						キャンセル
					</button>
					<button className='checkout-modal-button confirm' onClick={onConfirm}>
						会計を確定する
					</button>
				</div>
			</div>
		</div>
	);
}

function CheckoutProcessingMessage() {
	return (
		<div className='checkout-modal-overlay'>
			<div className='checkout-modal processing'>
				<div className='processing-spinner'>
					<LoadingSpinner message='会計処理中' />
				</div>
			</div>
		</div>
	);
}

function CheckoutSuccessMessage() {
	return (
		<div className='checkout-modal-overlay'>
			<div className='checkout-modal success'>
				<div className='checkout-modal-message'>
					<p>お会計を承りました。</p>
					<p>レジにて代金をお支払いください。</p>
				</div>
			</div>
		</div>
	);
}

interface CheckoutErrorMessageProps {
	onClose: () => void;
}
function CheckoutErrorMessage({ onClose }: CheckoutErrorMessageProps) {
	return (
		<div className='checkout-modal-overlay'>
			<div className='checkout-modal error'>
				<div className='checkout-modal-message'>
					<p>お会計の処理中に問題が発生しました。</p>
					<p>お近くの店員へお声がけください。</p>
				</div>
				<div className='checkout-modal-buttons'>
					<button className='checkout-modal-button confirm' onClick={onClose}>
						閉じる
					</button>
				</div>
			</div>
		</div>
	);
}

interface UnservedItemsConfirmModalProps {
	onProceed: () => void;
	onCancel: () => void;
}
function UnservedItemsConfirmModal({ onProceed, onCancel }: UnservedItemsConfirmModalProps) {
	return (
		<div className='checkout-modal-overlay'>
			<div className='checkout-modal unserved-warning'>
				<div className='checkout-modal-message'>
					<p>まだ配膳されていない商品があります。</p>
					<p>
						配膳されていない商品分の代金もお支払いいただくことになりますが、よろしいでしょうか？
					</p>
				</div>
				<div className='checkout-modal-buttons'>
					<button className='checkout-modal-button cancel' onClick={onCancel}>
						キャンセル
					</button>
					<button className='checkout-modal-button confirm warning' onClick={onProceed}>
						了承して会計へ進む
					</button>
				</div>
			</div>
		</div>
	);
}
async function fetchOrders(callback: (orders: Order[] | undefined) => void) {
	try {
		const response = await fetch(`${API_BASE_PATH_V1}/orders${API_SUFFIX_V1}`);
		if (!response.ok) {
			throw new Error('Failed to fetch orders');
		}

		const orders: Order[] = await response.json();

		await Promise.all(
			orders.map(async (order) => {
				const menuResponse = await fetch(
					`${API_BASE_PATH_V1}/menus/${order.menuId}${API_SUFFIX_V1}`,
				);
				if (!menuResponse.ok) {
					throw new Error('Failed to fetch menu item');
				}
				const menuItem: MenuItem = await menuResponse.json();
				order.menuItem = menuItem;

				order.options = await Promise.all(
					order.optionIds.map(async (optionId) => {
						const optionResponse = await fetch(
							`${API_BASE_PATH_V1}/options/${optionId}${API_SUFFIX_V1}`,
						);
						if (!optionResponse.ok) {
							Promise.reject(
								'Failed to fetch option item: orderId=' + order.orderId + ', optionId=' + optionId,
							);
						}
						const optionItem: Option = (await optionResponse.json()) as Option;
						return optionItem;
					}),
				);
			}),
		);
		callback(orders);
	} catch (error) {
		console.error('error!', error);
		callback(undefined);
	}
}
