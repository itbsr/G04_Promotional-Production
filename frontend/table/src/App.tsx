import type {} from 'react';
import { useEffect, useState } from 'react';
import type {} from './Cart';
import { Cart } from './Cart';
import type { SelectedCategory } from './Categories';
import { Categories } from './Categories';
import type {} from './constants';
import { API_BASE_PATH_V1, API_SUFFIX_V1 } from './constants';
import type { cartMenu } from './MenuList';
import { MenuList } from './MenuList';
import type { Category, MenuItem } from './models';
import {} from './models';
import type {} from './Option';
import { OptionSelect } from './Option';
import type {} from './OrderHistory';
import { OrderHistory } from './OrderHistory';
import type {} from './utilComponents';
import { Modal } from './utilComponents';

import './table.css';

export default function App() {
	// 商品リスト / null=ロード中,undefined=エラー
	const menuListState = useState<MenuItem[] | null | undefined>(null);
	const [menuList, setMenuList] = menuListState;

	// カテゴリ / null=ロード中, undefined=エラー
	const categoryListState = useState<Category[] | null | undefined>(null);
	const [categoriesList, setCategoriesList] = categoryListState;

	// カート内容
	const cartMenuState = useState<cartMenu[]>([]);

	// 選択中カテゴリ
	const selectedCategoryState = useState<SelectedCategory | null>(null);
	const [selectedCategory, setSelectedCategory] = selectedCategoryState;

	// オプション選択モーダル表示状態(商品選択中)
	const [selectedMenu, setSelectedMenu] = useState<MenuItem | null>(null);

	// カートモーダル表示状態
	const [isCartOpen, setIsCartOpen] = useState(false);

	// 注文履歴モーダル表示状態
	const [isOrderHistoryOpen, setIsOrderHistoryOpen] = useState(false);

	// メッセージモーダル内容。null=非表示
	const [message, setMessage] = useState<string | null>(null);

	function fetchMenus() {
		return fetch(API_BASE_PATH_V1 + '/menus' + API_SUFFIX_V1)
			.then((response) => {
				if (!response.headers.get('content-type')?.includes('application/json')) {
					throw new Error('Invalid content type');
				}
				return response.json();
			})
			.then((data) => {
				// データ格納
				setMenuList(data);
				return data;
			})
			.catch(() => {
				setMenuList(undefined);
			});
	}

	function fetchCategories() {
		return fetch(API_BASE_PATH_V1 + '/categories' + API_SUFFIX_V1)
			.then((response) => {
				if (!response.headers.get('content-type')?.includes('application/json')) {
					throw new Error('Invalid content type');
				}
				return response.json();
			})
			.then((data) => {
				// データ格納
				setCategoriesList(data);
				return data;
			})
			.catch(() => {
				setCategoriesList(undefined);
			});
	}

	// 初回データ取得
	useEffect(() => {
		const promises = [fetchMenus(), fetchCategories()];
		Promise.all(promises).then(([, categories]) => {
			if (!categories) return;
			setSelectedCategory({
				main: categories[0],
				sub: categories[0].children[0],
			});
		});
	}, []); // eslint-disable-line react-hooks/exhaustive-deps

	let menusToShow;
	if (menuList === undefined || categoriesList === undefined || selectedCategory === undefined) {
		menusToShow = undefined;
	} else if (menuList === null || categoriesList === null || selectedCategory === null) {
		menusToShow = null;
	} else {
		menusToShow =
			selectedCategory && categoriesList && menuList
				? menuList.filter((menu) => menu.categoryId === selectedCategory.sub.id)
				: undefined;
	}

	return (
		<>
			<div id='menu-container'>
				<Categories
					categoriesList={categoriesList}
					selectedCategory={selectedCategory}
					setSelectedCategory={setSelectedCategory}
				/>
				<div id='menu-accounting-container'>
					<div id='menu-list-container'>
						<MenuList menuList={menusToShow} onMenuSelect={(menu) => setSelectedMenu(menu)} />
					</div>

					<div id='accounting-container'>
						<div id='advertisement-container'>
							<img
								src='menuImages/advertisement.png'
								alt='広告画像'
								className='advertisement-image'
								onClick={() => {
									setSelectedMenu(
										menuList?.find((m) => m.name === 'ヤンニョムチキンバーガー') || null,
									);
								}}
							/>
						</div>
						<div id='accounting-buttons-container'>
							<button
								className={`accounting-button ${isCartOpen ? 'selected' : ''}`}
								id='btn-cart'
								onClick={() => setIsCartOpen(true)}
							>
								カート
							</button>
							<button
								className={`accounting-button ${isOrderHistoryOpen ? 'selected' : ''}`}
								id='btn-orders'
								onClick={() => setIsOrderHistoryOpen(true)}
							>
								注文履歴と会計
							</button>
						</div>
					</div>
				</div>

				{selectedMenu && (
					<Modal isOpen={!!selectedMenu}>
						<OptionSelect
							menu={selectedMenu}
							close={() => {
								setSelectedMenu(null);
								fetchMenus();
							}}
							cartMenuState={cartMenuState}
						/>
					</Modal>
				)}
				<Modal isOpen={isCartOpen}>
					<Cart
						cartMenuState={cartMenuState}
						onBackToMenu={() => setIsCartOpen(false)}
						onSuccess={() => {
							setIsCartOpen(false);
							setMessage('注文が完了しました！');
						}}
						onCancel={() => {
							setIsCartOpen(false);
							setMessage('注文確定をキャンセルしました。');
						}}
					/>
				</Modal>
				<Modal isOpen={message !== null}>
					<div id='order-success-modal-container' className='modal-disabled-background'>
						<div id='order-success-modal'>
							<h2 id='order-success-message'>{message}</h2>
							<button
								id='order-success-ok-button'
								onClick={() => {
									setMessage(null);
								}}
							>
								OK
							</button>
						</div>
					</div>
				</Modal>
				<Modal isOpen={isOrderHistoryOpen}>
					<OrderHistory onClose={() => setIsOrderHistoryOpen(false)} />
				</Modal>
			</div>
		</>
	);
}
