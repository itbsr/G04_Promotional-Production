/**
 * 売上管理システム - ユーティリティ関数
 */

/**
 * 日付をフォーマット
 * @param {Date} date - 日付オブジェクト
 * @param {string} format - フォーマット種別 ('full', 'date', 'time', 'short')
 * @returns {string} フォーマットされた日付文字列
 */
export function formatDate(date, format = 'date') {
	if (!date) {
		return '-';
	}
	const d = new Date(date);

	// 無効な日付の場合
	if (isNaN(d.getTime())) {
		return '-';
	}

	const year = d.getFullYear();
	const month = String(d.getMonth() + 1).padStart(2, '0');
	const day = String(d.getDate()).padStart(2, '0');
	const hours = String(d.getHours()).padStart(2, '0');
	const minutes = String(d.getMinutes()).padStart(2, '0');
	const seconds = String(d.getSeconds()).padStart(2, '0');

	const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
	const weekday = weekdays[d.getDay()];

	switch (format) {
		case 'full':
			return `${year}年${month}月${day}日(${weekday})`;
		case 'date':
			return `${year}-${month}-${day}`;
		case 'datetime':
			return `${year}-${month}-${day} ${hours}:${minutes}`;
		case 'time':
			return `${hours}:${minutes}:${seconds}`;
		case 'short':
			return `${month}/${day}`;
		case 'japanese':
			return `${year}年${month}月${day}日`;
		case 'monthDay':
			return `${month}月${day}日`;
		default:
			return `${year}-${month}-${day}`;
	}
}

/**
 * 金額をフォーマット
 * @param {number} amount - 金額
 * @param {boolean} includeYen - 円記号を含めるかどうか
 * @returns {string} フォーマットされた金額
 */
export function formatCurrency(amount, includeYen = true) {
	const value = amount == null || isNaN(amount) ? 0 : amount;
	const formatted = new Intl.NumberFormat('ja-JP').format(value);
	return includeYen ? `¥${formatted}` : formatted;
}

/**
 * パーセンテージをフォーマット
 * @param {number} value - 値
 * @param {number} decimals - 小数点以下の桁数
 * @returns {string} フォーマットされたパーセンテージ
 */
export function formatPercentage(value, decimals = 1) {
	return `${value.toFixed(decimals)}%`;
}

/**
 * 数値を短縮表記でフォーマット
 * @param {number} num - 数値
 * @returns {string} 短縮表記された数値
 */
export function formatCompactNumber(num) {
	if (num >= 1000000) {
		return `${(num / 1000000).toFixed(1)}M`;
	}
	if (num >= 1000) {
		return `${(num / 1000).toFixed(1)}K`;
	}
	return num.toString();
}

/**
 * 前期比を計算
 * @param {number} current - 現在の値
 * @param {number} previous - 前期の値
 * @returns {Object} 変化率と方向
 */
export function calculateChange(current, previous) {
	if (previous === 0) {
		return { percentage: current > 0 ? 100 : 0, direction: current > 0 ? 'up' : 'none' };
	}

	const change = ((current - previous) / previous) * 100;
	return {
		percentage: Math.abs(change),
		direction: change > 0 ? 'up' : change < 0 ? 'down' : 'none',
	};
}

/**
 * 期間の日付配列を生成
 * @param {Date} startDate - 開始日
 * @param {Date} endDate - 終了日
 * @returns {Array<Date>} 日付配列
 */
export function getDateRange(startDate, endDate) {
	const dates = [];
	const current = new Date(startDate);
	const end = new Date(endDate);

	while (current <= end) {
		dates.push(new Date(current));
		current.setDate(current.getDate() + 1);
	}

	return dates;
}

/**
 * 今日の日付を取得 (YYYY-MM-DD形式)
 * @returns {string} 今日の日付
 */
export function getToday() {
	return formatDate(new Date(), 'date');
}

/**
 * N日前の日付を取得
 * @param {number} days - 日数
 * @returns {string} N日前の日付 (YYYY-MM-DD形式)
 */
export function getDaysAgo(days) {
	const date = new Date();
	date.setDate(date.getDate() - days);
	return formatDate(date, 'date');
}

/**
 * 今月の初日を取得
 * @returns {string} 今月の初日 (YYYY-MM-DD形式)
 */
export function getFirstDayOfMonth() {
	const date = new Date();
	date.setDate(1);
	return formatDate(date, 'date');
}

/**
 * 今月の末日を取得
 * @returns {string} 今月の末日 (YYYY-MM-DD形式)
 */
export function getLastDayOfMonth() {
	const date = new Date();
	date.setMonth(date.getMonth() + 1);
	date.setDate(0);
	return formatDate(date, 'date');
}

/**
 * デバウンス関数
 * @param {Function} func - 実行する関数
 * @param {number} wait - 待機時間（ミリ秒）
 * @returns {Function} デバウンスされた関数
 */
export function debounce(func, wait) {
	let timeout;
	return function executedFunction(...args) {
		const later = () => {
			clearTimeout(timeout);
			func(...args);
		};
		clearTimeout(timeout);
		timeout = setTimeout(later, wait);
	};
}

/**
 * スロットル関数
 * @param {Function} func - 実行する関数
 * @param {number} limit - 制限時間（ミリ秒）
 * @returns {Function} スロットルされた関数
 */
export function throttle(func, limit) {
	let inThrottle;
	return function (...args) {
		if (!inThrottle) {
			func.apply(this, args);
			inThrottle = true;
			setTimeout(() => (inThrottle = false), limit);
		}
	};
}

/**
 * トースト通知を表示
 * @param {string} message - メッセージ
 * @param {string} type - タイプ ('success', 'error', 'warning', 'info')
 * @param {number} duration - 表示時間（ミリ秒）
 */
export function showToast(message, type = 'info', duration = 3000) {
	const container = document.getElementById('toast-container');
	if (!container) return;

	const toast = document.createElement('div');
	toast.className = `toast ${type}`;
	toast.textContent = message;

	container.appendChild(toast);

	// 自動で削除
	setTimeout(() => {
		toast.style.opacity = '0';
		toast.style.transform = 'translateX(100%)';
		setTimeout(() => toast.remove(), 300);
	}, duration);
}

/**
 * HTMLをエスケープ
 * @param {string} text - テキスト
 * @returns {string} エスケープされたテキスト
 */
export function escapeHtml(text) {
	const div = document.createElement('div');
	div.textContent = text;
	return div.innerHTML;
}

/**
 * HTML要素を作成
 * @param {string} tag - タグ名
 * @param {Object} attributes - 属性
 * @param {string|HTMLElement|Array} children - 子要素
 * @returns {HTMLElement} 作成された要素
 */
export function createElement(tag, attributes = {}, children = null) {
	const element = document.createElement(tag);

	Object.entries(attributes).forEach(([key, value]) => {
		if (key === 'className') {
			element.className = value;
		} else if (key === 'style' && typeof value === 'object') {
			Object.assign(element.style, value);
		} else if (key.startsWith('on') && typeof value === 'function') {
			element.addEventListener(key.slice(2).toLowerCase(), value);
		} else {
			element.setAttribute(key, value);
		}
	});

	if (children) {
		if (Array.isArray(children)) {
			children.forEach((child) => {
				if (typeof child === 'string') {
					element.appendChild(document.createTextNode(child));
				} else if (child instanceof HTMLElement) {
					element.appendChild(child);
				}
			});
		} else if (typeof children === 'string') {
			element.textContent = children;
		} else if (children instanceof HTMLElement) {
			element.appendChild(children);
		}
	}

	return element;
}
