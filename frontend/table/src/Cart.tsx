import type {} from 'react';
import { useEffect, useRef, useState } from 'react';
import type { cartMenu } from './MenuList';
import {} from './MenuList';
import type {} from './constants';
import { API_BASE_PATH_V1, API_SUFFIX_V1 } from './constants';
import type {} from './EmptyCart';
import { EmptyCart } from './EmptyCart';

interface CartProps {
	cartMenuState: [cartMenu[], React.Dispatch<React.SetStateAction<cartMenu[]>>];
	onBackToMenu: () => void;
	onSuccess: () => void;
	onCancel: () => void;
}

const SUBMIT_TIMEOUT_MS = 5000;
export function Cart({ cartMenuState, onBackToMenu, onSuccess, onCancel }: CartProps) {
	const [state, setState] = useState<null | 'submit-timeout' | 'registering'>(null);

	const [cartItems, setCartItems] = cartMenuState;
	const cartAnimation1Ref = useRef<SVGAnimateElement>(null);
	const cartAnimation2Ref = useRef<SVGAnimateElement>(null);

	const submitTimeoutRef = useRef<number>(0);
	useEffect(() => {
		return () => clearTimeout(submitTimeoutRef.current);
	}, []);

	const totalPrice = cartItems.reduce(
		(sum, item) =>
			sum + item.menu.price + item.selectedOptions.reduce((optSum, opt) => optSum + opt.price, 0),
		0,
	);

	return (
		<div className='cart-container'>
			<div className='cart-list-container'>
				{cartItems.length === 0 ? (
					<EmptyCart />
				) : (
					cartItems.map((menuItem, idx) => (
						<CartItem
							key={menuItem.id}
							cartItem={menuItem}
							onDelete={() => {
								if (state === null) setCartItems(cartItems.filter((_, i) => i !== idx));
							}}
						/>
					))
				)}
			</div>
			<div className='cart-back-submit-container'>
				<button
					disabled={state === 'registering'}
					onClick={() => {
						switch (state) {
							case null:
								onBackToMenu();
								break;
							case 'submit-timeout':
								onCancel();
								break;
						}
					}}
				>
					{state === 'submit-timeout' ? '注文をキャンセル' : 'メニューに戻る'}
				</button>
				<div className='price-submit-container'>
					<p>カート内合計金額</p>
					<p>{totalPrice}円</p>
					<button
						disabled={cartItems.length === 0 || state === 'registering'}
						className={`cart-button-submit ${state !== null ? 'submitted' : ''}`}
						style={{ position: 'relative' }}
						onClick={() => {
							switch (state) {
								case null: {
									cartAnimation1Ref.current?.beginElement();
									cartAnimation2Ref.current?.beginElement();
									setState('submit-timeout');
									submitTimeoutRef.current = setTimeout(() => {
										setState('registering');
										fetch(API_BASE_PATH_V1 + '/orders' + API_SUFFIX_V1, {
											method: 'POST',
											headers: {
												'Content-Type': 'application/json',
											},
											body: JSON.stringify({
												orders: cartItems.map((item) => ({
													menuId: item.menu.id,
													optionIds: item.selectedOptions.map((opt) => opt.id),
												})),
											}),
										})
											.then((response) => {
												return response.ok;
											})
											.then((success) => {
												if (success) {
													setCartItems([]);
													onSuccess();
												} else {
													alert('注文の送信に失敗しました。');
													onBackToMenu();
												}
											});
									}, SUBMIT_TIMEOUT_MS);
									break;
								}
								case 'submit-timeout': {
									onCancel();
									break;
								}
							}
						}}
					>
						<div className='cart-button-submitting-overlay'>注文中......</div>
						<div className='cart-button-submit-content'>
							<svg height={'80%'} viewBox='0 0 35 28'>
								{/* head */}
								<circle cx={9} cy={3} r={2} />
								{/* body */}
								<path d='M9 7.5 l0 6' stroke='black' strokeWidth={4} strokeLinecap='round' />
								{/* arm */}
								<path
									d='M12 8 l2.5 1.5 l2.75 -.5'
									fill='none'
									stroke='black'
									strokeWidth={1.5}
									strokeLinecap='round'
								/>
								{/* legs */}
								<path
									d='M9.75 15 l1.25 3.5 l1 3.75'
									fill='none'
									stroke='black'
									strokeWidth={2.5}
									strokeLinecap='round'
								>
									<animate
										ref={cartAnimation1Ref}
										begin={'indefinite'}
										attributeName='d'
										dur='.5s'
										repeatCount='indefinite'
										values='
										M9.75 15 l1.25 3.5 l1 3.75;
										M9.75 15 l1.5 3.75 l.25 3.75;
										M9.25 15 l.75 3.75 l-.5 3.75;
										M8.75 15 l-.5 3.75 l-1.5 3.75;
										M8.25 15 l-1.25 3.75 l-1.25 3.75;
										M9.25 15 l0 3.5 l-3.5 3;
										M9.75 15 l1.25 3.5 l1 3.75
										'
									/>
								</path>
								<path
									d='M8.75 15 l-.5 3.75 l-1.5 3.75'
									fill='none'
									stroke='black'
									strokeWidth={2.5}
									strokeLinecap='round'
								>
									<animate
										ref={cartAnimation2Ref}
										begin={'indefinite'}
										attributeName='d'
										dur='.5s'
										repeatCount='indefinite'
										values='
										M8.75 15 l-.5 3.75 l-1.5 3.75;
										M8.25 15 l-1.25 3.75 l-1.25 3.75;
										M9.25 15 l0 3.5 l-3.5 3;
										M9.75 15 l1.25 3.5 l1 3.75;
										M9.75 15 l1.5 3.75 l.25 3.75;
										M9.25 15 l.75 3.75 l-.5 3.75;
										M8.75 15 l-.5 3.75 l-1.5 3.75
										'
									/>
								</path>

								{/* cart */}
								<path
									transform='translate(18, 8)'
									d='M0 2.5A.5.5 0 0 1 .5 2H2a.5.5 0 0 1 .485.379L2.89 4H14.5a.5.5 0 0 1 .485.621l-1.5 6A.5.5 0 0 1 13 11H4a.5.5 0 0 1-.485-.379L1.61 3H.5a.5.5 0 0 1-.5-.5M3.14 5l.5 2H5V5zM6 5v2h2V5zm3 0v2h2V5zm3 0v2h1.36l.5-2zm1.11 3H12v2h.61zM11 8H9v2h2zM8 8H6v2h2zM5 8H3.89l.5 2H5zm0 5a1 1 0 1 0 0 2 1 1 0 0 0 0-2m-2 1a2 2 0 1 1 4 0 2 2 0 0 1-4 0m9-1a1 1 0 1 0 0 2 1 1 0 0 0 0-2m-2 1a2 2 0 1 1 4 0 2 2 0 0 1-4 0'
								/>
							</svg>
							<div>注文する</div>
						</div>
					</button>
				</div>
			</div>
		</div>
	);
}

interface CartItemProps {
	cartItem: cartMenu;
	onDelete: () => void;
}
function CartItem({ cartItem, onDelete }: CartItemProps) {
	const price =
		cartItem.menu.price + cartItem.selectedOptions.reduce((sum, opt) => sum + opt.price, 0);

	return (
		<div className='cart-item-container'>
			<img src={cartItem.menu.image} alt={cartItem.menu.name} className='menu-image' />
			<div className='menu-name-options-container'>
				<p className='menu-name'>{cartItem.menu.name}</p>
				{cartItem.selectedOptions.length > 0 && (
					<div className='order-item-options'>
						<p className='order-options-label'>オプション:</p>
						{cartItem.selectedOptions.map((opt) => (
							<span key={opt.id} className='order-option-item'>
								{opt.name}
							</span>
						))}
					</div>
				)}
			</div>
			<div className='delete-price-container'>
				<button className='delete-button' onClick={onDelete}>
					カートから削除
				</button>
				<div className='total-price'>価格 : ￥{price}</div>
			</div>
		</div>
	);
}
