/**
 * 売上管理システム - API通信モジュール
 *
 * すべてのAPI通信を一元管理
 */

/**
 * APIのベースURL
 */
const API_BASE_URL = '/G04PromotionalProduction/api/v1';

/**
 * HTTPリクエストを送信
 * @param {string} endpoint - エンドポイント
 * @param {Object} options - fetch オプション
 * @returns {Promise<any>} レスポンスデータ
 */
async function request(endpoint, options = {}) {
	const url = `${API_BASE_URL}${endpoint}`;

	const defaultOptions = {
		headers: {
			'Content-Type': 'application/json',
		},
	};

	const mergedOptions = {
		...defaultOptions,
		...options,
		headers: {
			...defaultOptions.headers,
			...options.headers,
		},
	};

	try {
		const response = await fetch(url, mergedOptions);

		if (!response.ok) {
			const error = new Error(`HTTP error! status: ${response.status}`);
			error.status = response.status;
			error.response = response;
			throw error;
		}

		// レスポンスが空の場合
		const contentType = response.headers.get('content-type');
		if (!contentType || !contentType.includes('application/json')) {
			return null;
		}

		return await response.json();
	} catch (error) {
		console.error(`API request failed: ${endpoint}`, error);
		throw error;
	}
}

/**
 * GETリクエスト
 * @param {string} endpoint - エンドポイント
 * @param {Object} params - クエリパラメータ
 * @returns {Promise<any>} レスポンスデータ
 */
export async function get(endpoint, params = {}) {
	const queryString = new URLSearchParams(params).toString();
	const url = queryString ? `${endpoint}?${queryString}` : endpoint;
	return request(url, { method: 'GET' });
}

/**
 * POSTリクエスト
 * @param {string} endpoint - エンドポイント
 * @param {Object} data - リクエストボディ
 * @returns {Promise<any>} レスポンスデータ
 */
export async function post(endpoint, data = {}) {
	return request(endpoint, {
		method: 'POST',
		body: JSON.stringify(data),
	});
}

/**
 * PUTリクエスト
 * @param {string} endpoint - エンドポイント
 * @param {Object} data - リクエストボディ
 * @returns {Promise<any>} レスポンスデータ
 */
export async function put(endpoint, data = {}) {
	return request(endpoint, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
}

/**
 * DELETEリクエスト
 * @param {string} endpoint - エンドポイント
 * @returns {Promise<any>} レスポンスデータ
 */
export async function del(endpoint) {
	return request(endpoint, { method: 'DELETE' });
}

// =====================================
// 売上関連API
// =====================================

/**
 * 日別売上を取得
 * @param {string} date - 日付 (YYYY-MM-DD)
 * @returns {Promise<Object>} 売上データ
 */
export async function getDailySales(date) {
	return get('/sales/daily', { date });
}

/**
 * 月別売上を取得
 * @param {number} year - 年
 * @param {number} month - 月
 * @returns {Promise<Object>} 売上データ
 */
export async function getMonthlySales(year, month) {
	return get('/sales/monthly', { year, month });
}

/**
 * 期間指定で売上を取得
 * @param {string} startDate - 開始日 (YYYY-MM-DD)
 * @param {string} endDate - 終了日 (YYYY-MM-DD)
 * @returns {Promise<Object>} 売上データ
 */
export async function getSalesByPeriod(startDate, endDate) {
	return get('/sales', { startDate, endDate });
}

/**
 * 商品別売上を取得
 * @param {string} startDate - 開始日 (YYYY-MM-DD)
 * @param {string} endDate - 終了日 (YYYY-MM-DD)
 * @returns {Promise<Array>} 商品別売上データ
 */
export async function getProductSales(startDate, endDate) {
	return get('/sales/products', { startDate, endDate });
}

/**
 * カテゴリ別売上を取得
 * @param {string} startDate - 開始日 (YYYY-MM-DD)
 * @param {string} endDate - 終了日 (YYYY-MM-DD)
 * @returns {Promise<Array>} カテゴリ別売上データ
 */
export async function getCategorySales(startDate, endDate) {
	return get('/sales/categories', { startDate, endDate });
}

// =====================================
// 注文関連API
// =====================================

/**
 * 注文一覧を取得
 * @param {Object} params - クエリパラメータ
 * @returns {Promise<Array>} 注文一覧
 */
export async function getOrders(params = {}) {
	return get('/orders', params);
}

/**
 * 注文詳細を取得
 * @param {string} orderId - 注文ID
 * @returns {Promise<Object>} 注文詳細
 */
export async function getOrderDetail(orderId) {
	return get(`/orders/${orderId}`);
}

// =====================================
// ダッシュボード関連API
// =====================================

/**
 * ダッシュボードサマリーを取得
 * @returns {Promise<Object>} ダッシュボードデータ
 */
export async function getDashboardSummary() {
	return get('/sales/summary');
}

/**
 * 今日の売上を取得
 * @returns {Promise<Object>} 今日の売上データ
 */
export async function getTodaySales() {
	const today = new Date().toISOString().split('T')[0];
	return getDailySales(today);
}
