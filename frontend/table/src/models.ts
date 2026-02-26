export interface MenuItem {
	id: number;
	deletedAt: Date | null;
	name: string;
	categoryId: number;
	price: number;
	image: string;
	optionIds: number[];
}

export interface Category {
	id: number;
	name: string;
	children: Category[];
}

export interface Option {
	id: number;
	menuId: number;
	name: string;
	price: number;
}

export interface Order {
	// Orders APIから返されるデータ
	orderId: string;
	menuId: number;
	statusId: number;
	statusName: string;
	optionIds: number[];

	// Orders APIから取得後に追加で取得したデータの格納場所
	menuItem: MenuItem;
	options: Option[];
}
