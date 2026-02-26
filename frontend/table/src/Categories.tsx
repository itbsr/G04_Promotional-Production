import type {} from './ErrorScreen';
import { ErrorScreen } from './ErrorScreen';
import type { Category } from './models';
import {} from './models';

export interface SelectedCategory {
	main: Category;
	sub: Category;
}

interface CategoriesProps {
	categoriesList: Category[] | null | undefined;
	selectedCategory: SelectedCategory | null | undefined;
	setSelectedCategory: (category: SelectedCategory) => void;
}
export function Categories({
	categoriesList,
	selectedCategory,
	setSelectedCategory,
}: CategoriesProps) {
	if (categoriesList === undefined || selectedCategory === undefined)
		return (
			<div id='menu-categories-container'>
				<ErrorScreen message='カテゴリーの取得に失敗しました' />
			</div>
		);

	if (categoriesList === null || selectedCategory === null)
		return (
			<div id='menu-categories-container'>
				{/* <LoadingSpinner message='カテゴリーを読み込み中' /> */}
			</div>
		);

	return (
		<div id='menu-categories-container'>
			<div className='category-row' id='main-categories-container'>
				{categoriesList?.map((mainCategory) => (
					<div
						key={mainCategory.id}
						onClick={() => {
							setSelectedCategory({
								main: mainCategory,
								sub: mainCategory.children[0],
							});
						}}
						className={
							'category-item' +
							(selectedCategory.main.id === mainCategory.id ? ' selected-category' : '')
						}
					>
						{mainCategory.name}
					</div>
				))}
			</div>
			<div className='category-row' id='sub-categories-container'>
				{selectedCategory.main.children.map((subCategory) => (
					<div
						key={subCategory.id}
						onClick={() => {
							setSelectedCategory({
								main: selectedCategory.main,
								sub: subCategory,
							});
						}}
						className={
							'category-item' +
							(selectedCategory.sub.id === subCategory.id ? ' selected-category' : '')
						}
					>
						{subCategory.name}
					</div>
				))}
			</div>
		</div>
	);
}
