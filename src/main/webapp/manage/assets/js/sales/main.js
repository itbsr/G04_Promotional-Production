/**
 * 売上管理システム - メインエントリーポイント
 *
 * モジュール構成:
 * - main.js: アプリケーションの初期化とルーティング
 * - api.js: API通信の共通処理
 * - utils.js: ユーティリティ関数
 * - pages/: 各ページのモジュール
 */

import { initRouter, navigateTo } from './router.js';
import { formatDate } from './utils.js';

/**
 * アプリケーションの初期化
 */
class SalesApp {
	constructor() {
		this.currentPage = 'dashboard';
		this.isInitialized = false;
	}

	/**
	 * アプリケーションを初期化
	 */
	async init() {
		if (this.isInitialized) return;

		this.setupEventListeners();
		this.updateCurrentDate();
		this.startDateUpdater();

		// ルーターの初期化
		initRouter();

		// 初期ページを表示
		await navigateTo('dashboard');

		this.hideLoading();
		this.isInitialized = true;

		console.log('売上管理システムが初期化されました');
	}

	/**
	 * イベントリスナーを設定
	 */
	setupEventListeners() {
		// サイドバーナビゲーション
		const navLinks = document.querySelectorAll('.nav-link');
		navLinks.forEach((link) => {
			link.addEventListener('click', (e) => {
				e.preventDefault();
				const page = link.dataset.page;
				if (page) {
					this.handleNavigation(page, navLinks, link);
				}
			});
		});

		// モバイルメニュートグル
		const menuToggle = document.getElementById('menu-toggle');
		if (menuToggle) {
			menuToggle.addEventListener('click', () => this.toggleSidebar());
		}

		// サイドバー外クリックで閉じる（モバイル）
		document.addEventListener('click', (e) => {
			const sidebar = document.querySelector('.sidebar');
			const menuToggle = document.getElementById('menu-toggle');

			if (
				window.innerWidth <= 1024 &&
				sidebar.classList.contains('open') &&
				!sidebar.contains(e.target) &&
				!menuToggle.contains(e.target)
			) {
				this.closeSidebar();
			}
		});

		// ウィンドウリサイズ時の処理
		window.addEventListener('resize', () => {
			if (window.innerWidth > 1024) {
				this.closeSidebar();
			}
		});
	}

	/**
	 * ナビゲーション処理
	 * @param {string} page - ページ名
	 * @param {NodeList} navLinks - ナビゲーションリンク
	 * @param {HTMLElement} activeLink - アクティブなリンク
	 */
	async handleNavigation(page, navLinks, activeLink) {
		if (page === this.currentPage) return;

		// アクティブ状態を更新
		navLinks.forEach((link) => link.classList.remove('active'));
		activeLink.classList.add('active');

		// ページタイトルを更新
		this.updatePageTitle(page);

		// ページを読み込み
		this.showLoading();
		await navigateTo(page);
		this.hideLoading();

		this.currentPage = page;

		// モバイルの場合はサイドバーを閉じる
		if (window.innerWidth <= 1024) {
			this.closeSidebar();
		}
	}

	/**
	 * ページタイトルを更新
	 * @param {string} page - ページ名
	 */
	updatePageTitle(page) {
		const titles = {
			dashboard: 'ダッシュボード',
			daily: '日別売上',
			monthly: '月別売上',
			products: '商品別売上',
			categories: 'カテゴリ別売上',
			orders: '注文履歴',
		};

		const titleElement = document.getElementById('page-title');
		if (titleElement) {
			titleElement.textContent = titles[page] || 'ダッシュボード';
		}
	}

	/**
	 * 現在の日付を更新
	 */
	updateCurrentDate() {
		const dateDisplay = document.getElementById('current-date');
		if (dateDisplay) {
			dateDisplay.textContent = formatDate(new Date(), 'full');
		}
	}

	/**
	 * 日付更新タイマーを開始
	 */
	startDateUpdater() {
		// 1分ごとに日付を更新
		setInterval(() => this.updateCurrentDate(), 60000);
	}

	/**
	 * サイドバーをトグル
	 */
	toggleSidebar() {
		const sidebar = document.querySelector('.sidebar');
		sidebar.classList.toggle('open');
		this.updateOverlay(sidebar.classList.contains('open'));
	}

	/**
	 * サイドバーを閉じる
	 */
	closeSidebar() {
		const sidebar = document.querySelector('.sidebar');
		sidebar.classList.remove('open');
		this.updateOverlay(false);
	}

	/**
	 * オーバーレイを更新
	 * @param {boolean} show - 表示するかどうか
	 */
	updateOverlay(show) {
		let overlay = document.querySelector('.sidebar-overlay');

		if (show) {
			if (!overlay) {
				overlay = document.createElement('div');
				overlay.className = 'sidebar-overlay';
				document.body.appendChild(overlay);
			}
			overlay.classList.add('active');
		} else if (overlay) {
			overlay.classList.remove('active');
		}
	}

	/**
	 * ローディングを表示
	 */
	showLoading() {
		const loading = document.getElementById('loading');
		if (loading) {
			loading.classList.remove('hidden');
		}
	}

	/**
	 * ローディングを非表示
	 */
	hideLoading() {
		const loading = document.getElementById('loading');
		if (loading) {
			loading.classList.add('hidden');
		}
	}
}

// DOMContentLoaded時に初期化
document.addEventListener('DOMContentLoaded', () => {
	const app = new SalesApp();
	app.init();
});

// グローバルにアプリケーションインスタンスを公開（デバッグ用）
window.SalesApp = SalesApp;
