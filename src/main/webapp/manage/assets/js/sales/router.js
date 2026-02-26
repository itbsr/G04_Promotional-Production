/**
 * 売上管理システム - ルーター
 *
 * ページ遷移とコンテンツの動的読み込みを管理
 */

import { renderCategorySales } from './pages/categories.js';
import { renderDailySales } from './pages/daily.js';
import { renderDashboard } from './pages/dashboard.js';
import { renderMonthlySales } from './pages/monthly.js';
import { renderOrderHistory } from './pages/orders.js';
import { renderProductSales } from './pages/products.js';

/**
 * ページ定義
 */
const pages = {
	dashboard: {
		title: 'ダッシュボード',
		render: renderDashboard,
	},
	daily: {
		title: '日別売上',
		render: renderDailySales,
	},
	monthly: {
		title: '月別売上',
		render: renderMonthlySales,
	},
	products: {
		title: '商品別売上',
		render: renderProductSales,
	},
	categories: {
		title: 'カテゴリ別売上',
		render: renderCategorySales,
	},
	orders: {
		title: '注文履歴',
		render: renderOrderHistory,
	},
};

/**
 * 現在のページ
 */
let currentPage = null;

/**
 * ルーターを初期化
 */
export function initRouter() {
	// ブラウザの戻る/進むに対応
	window.addEventListener('popstate', (e) => {
		if (e.state && e.state.page) {
			navigateTo(e.state.page, false);
		}
	});
}

/**
 * 指定されたページに遷移
 * @param {string} pageName - ページ名
 * @param {boolean} pushState - 履歴に追加するかどうか
 */
export async function navigateTo(pageName, pushState = true) {
	const page = pages[pageName];

	if (!page) {
		console.error(`ページが見つかりません: ${pageName}`);
		return;
	}

	const contentArea = document.getElementById('content-area');
	if (!contentArea) {
		console.error('コンテンツエリアが見つかりません');
		return;
	}

	try {
		// ページをレンダリング
		const content = await page.render();

		// ローディング以外のコンテンツをクリア
		const loading = document.getElementById('loading');
		contentArea.innerHTML = '';
		if (loading) {
			contentArea.appendChild(loading);
		}

		// 新しいコンテンツを追加
		if (typeof content === 'string') {
			const wrapper = document.createElement('div');
			wrapper.innerHTML = content;
			contentArea.appendChild(wrapper);
		} else if (content instanceof HTMLElement) {
			contentArea.appendChild(content);
		}

		// 履歴に追加
		if (pushState && pageName !== currentPage) {
			history.pushState({ page: pageName }, page.title, `#${pageName}`);
		}

		currentPage = pageName;
	} catch (error) {
		console.error(`ページの読み込みに失敗しました: ${pageName}`, error);
		contentArea.innerHTML = `
			<div class="empty-state">
				<div class="empty-state-icon">⚠️</div>
				<h3 class="empty-state-title">エラーが発生しました</h3>
				<p class="empty-state-description">ページの読み込みに失敗しました。再度お試しください。</p>
			</div>
		`;
	}
}

/**
 * 現在のページ名を取得
 * @returns {string} 現在のページ名
 */
export function getCurrentPage() {
	return currentPage;
}
