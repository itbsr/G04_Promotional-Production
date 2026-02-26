import type {} from './ErrorScreen';
import { ErrorScreen } from './ErrorScreen';
import type {} from './LoadingScreen';
import { LoadingSpinner } from './LoadingScreen';
import type { MenuItem, Option } from './models';
import {} from './models';

export interface cartMenu {
	id: number;
	menu: MenuItem;
	selectedOptions: Option[];
}

interface MenuListProps {
	menuList: MenuItem[] | null | undefined;
	onMenuSelect: (menu: MenuItem) => void;
}
export function MenuList({ menuList, onMenuSelect }: MenuListProps) {
	if (menuList === null) {
		return <LoadingSpinner message='メニューを読み込み中' />;
	} else if (menuList === undefined) {
		return <ErrorScreen message='メニューの取得に失敗しました' />;
	}

	return (
		<>
			{menuList.map((item) => (
				<div className='menu-item' key={item.id} onClick={() => onMenuSelect(item)}>
					<img src={item.image} alt={item.name} className='menu-item-image' />
					<div className='menu-item-name-price'>
						<p>{item.name}</p>
						<p>{item.price}円</p>
					</div>
				</div>
			))}
		</>
	);
}
