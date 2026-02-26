import type {} from 'react';
import { useEffect, useState } from 'react';
import type {} from './constants';
import { API_BASE_PATH_V1, API_SUFFIX_V1 } from './constants';
import type {} from './ErrorScreen';
import { ErrorScreen } from './ErrorScreen';
import type {} from './LoadingScreen';
import { LoadingSpinner } from './LoadingScreen';
import type { cartMenu } from './MenuList';
import {} from './MenuList';
import type { MenuItem, Option } from './models';
import {} from './models';

interface optionSelectProps {
	menu: MenuItem;
	close: () => void;
	cartMenuState: [cartMenu[], React.Dispatch<React.SetStateAction<cartMenu[]>>];
}
export function OptionSelect({ menu, close, cartMenuState }: optionSelectProps) {
	const [options, setOptions] = useState<Option[] | null | undefined>(null);
	useEffect(() => {
		const promises = menu?.optionIds.map((optionId) =>
			fetch(API_BASE_PATH_V1 + `/options/${optionId}${API_SUFFIX_V1}`).then((res) =>
				res.ok ? res.json() : Error('Failed to fetch option'),
			),
		);
		Promise.all(promises ?? [])
			.then((options) => {
				const isError = options.some((opt) => opt instanceof Error);
				setOptions(isError ? undefined : options);
			})
			.catch(() => {
				setOptions(undefined);
			});
	}, []); // eslint-disable-line react-hooks/exhaustive-deps

	const selectedOptionState = useState<Option[]>([]);
	const [selectedOptions] = selectedOptionState;
	const [cartMenu, setCartMenu] = cartMenuState;

	return (
		<div className='option-select-container'>
			<div className='menu-description-option-container'>
				<>
					<div className='menu-description'>
						<div style={{ flexGrow: 1 }}></div>
						<div className='menu-image-container'>
							<img src={menu.image} alt={menu.name} />
						</div>
						<div className='menu-name'>{menu.name}</div>
						<div className='menu-price'>
							<div>合計価格 :</div>
							<div className='price-label'>
								{menu.price + selectedOptions.reduce((sum, opt) => sum + opt.price, 0)}円
							</div>
						</div>
					</div>
					<div className='option-list-container'>
						{options === undefined ? (
							<ErrorScreen
								message={'オプション情報の取得に失敗しました。お手数ですが再度お試しください。'}
							/>
						) : options === null ? (
							<LoadingSpinner message='オプションを読み込み中' />
						) : (
							<OptionList options={options} selectedOptionsState={selectedOptionState} />
						)}
					</div>
				</>
			</div>
			<div className='back-and-submit-container'>
				<button onClick={close}>メニューに戻る</button>
				<div></div>
				<button
					onClick={() => {
						if (menu)
							setCartMenu([
								...cartMenu,
								{ id: cartMenu.length, menu: menu, selectedOptions: selectedOptions },
							]);
						close();
					}}
				>
					カートへ追加
				</button>
			</div>
		</div>
	);
}

interface OptionListProps {
	options: Option[];
	selectedOptionsState: [Option[], React.Dispatch<React.SetStateAction<Option[]>>];
}
function OptionList({ options, selectedOptionsState }: OptionListProps) {
	const [selectedOptions, setSelectedOptions] = selectedOptionsState;

	return (
		<>
			<div className='option-list-title'>オプションを選択してください</div>
			<div className='option-buttons'>
				{options ? (
					options.map((opt) => (
						<button
							className={
								'option-item' + (selectedOptions.some((o) => o.id === opt.id) ? ' selected' : '')
							}
							key={opt.id}
							onClick={() => {
								if (!selectedOptions.some((o) => o.id === opt.id))
									setSelectedOptions([...selectedOptions, opt]);
								else setSelectedOptions(selectedOptions.filter((o) => o.id !== opt.id));
							}}
						>
							<OptionStatusIcon selected={selectedOptions.some((o) => o.id === opt.id)} />
							<p className='option-name'>
								{opt.name}({opt.price}円)
							</p>
						</button>
					))
				) : (
					<p>error</p>
				)}
				{/* TODO: Error表示実装*/}
			</div>
		</>
	);
}

function OptionStatusIcon({ selected }: { selected: boolean }) {
	return (
		<svg
			className={`
				option-cart-icon
				${selected ? 'selected' : ''}
				`}
			xmlns='http://www.w3.org/2000/svg'
			fill='currentColor'
			viewBox='0 0 16 16'
		>
			<path d='M.5 1a.5.5 0 0 0 0 1h1.11l.401 1.607 1.498 7.985A.5.5 0 0 0 4 12h1a2 2 0 1 0 0 4 2 2 0 0 0 0-4h7a2 2 0 1 0 0 4 2 2 0 0 0 0-4h1a.5.5 0 0 0 .491-.408l1.5-8A.5.5 0 0 0 14.5 3H2.89l-.405-1.621A.5.5 0 0 0 2 1zm3.915 10L3.102 4h10.796l-1.313 7zM6 14a1 1 0 1 1-2 0 1 1 0 0 1 2 0m7 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0' />
			<path
				className='plus-in-cart'
				d='M9 5.5a.5.5 0 0 0-1 0V7H6.5a.5.5 0 0 0 0 1H8v1.5a.5.5 0 0 0 1 0V8h1.5a.5.5 0 0 0 0-1H9z'
			/>
			<path
				className='check-in-cart'
				d='M11.354 6.354a.5.5 0 0 0-.708-.708L8 8.293 6.854 7.146a.5.5 0 1 0-.708.708l1.5 1.5a.5.5 0 0 0 .708 0z'
			/>
		</svg>
	);
}
