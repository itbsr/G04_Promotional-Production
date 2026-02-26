<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" /><meta
			name="viewport"
			content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
		/>
<title>チェックアウト</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/checkout.css">

<!-- PWA Settings -->
<link rel="manifest" href="manifest.json" />
<meta name="theme-color" content="#283593" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="default" />
<meta name="apple-mobile-web-app-title" content="G04レジ" />
</head>
<body>
	<div class="content">
		<div class="header">
			<div class="header-left">
				<c:if test="${not empty vm}">
					<button type="button" class="back-btn header-back-btn"
						onclick="goBack()">← 戻る</button>
				</c:if>
				<h1>🛒 レジ会計システム</h1>
			</div>
			<div class="status-bar">
				<c:if test="${not empty vm}">
					<button type="button" class="print-btn" onclick="printReceipt()">🖨️ 印刷</button>
				</c:if>
				<c:choose>
					<c:when test="${empty vm}">
						<span class="status">ステップ 1/2：顧客ID入力</span>
					</c:when>
					<c:otherwise>
						<span class="status">ステップ 2/2：会計確認</span>
					</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="container">
			<c:choose>
				<c:when test="${empty vm}">
					<!-- Customer ID Input Section - Two Column Layout -->
					<div class="keypad-container">
						<!-- Left Column: Display and Instructions -->
						<div class="keypad-left">
							<div class="display-panel">
								<div class="display-header">
									<h2>📱 顧客ID入力</h2>
								</div>

								<div class="input-display-large">
									<div class="display-value" id="displayValueLarge">——————</div>
									<div class="display-hint">お客様番号を入力</div>
								</div>
								<c:if test="${not empty message}">
									<div class="error-message">⚠️ ${message}</div>
								</c:if>
							</div>
						</div>

						<!-- Right Column: Keypad -->
						<div class="keypad-right">
							<div class="keypad-panel">
								<form method="post" action="" id="checkoutForm">
									<input type="text" id="customerIdDisplay" class="input-display"
										readonly placeholder="" style="display: none;" /> <input
										type="hidden" name="customerId" id="customerIdInput" />

									<div class="keypad">
										<button type="button" class="keypad-btn"
											onclick="addDigit('1')">1</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('2')">2</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('3')">3</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('4')">4</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('5')">5</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('6')">6</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('7')">7</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('8')">8</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('9')">9</button>
										<button type="button" class="keypad-btn clear-btn"
											onclick="clearAll()">C</button>
										<button type="button" class="keypad-btn"
											onclick="addDigit('0')">0</button>
										<button type="button" class="keypad-btn delete-btn"
											onclick="deleteDigit()">←</button>
									</div>

									<button type="submit" class="submit-btn" id="submitBtn"
										disabled>💳 注文内容を確認</button>
								</form>
							</div>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<!-- Receipt Display Section - Two Column Layout -->
					<div class="receipt-container">
						<!-- Left Column: Receipt Details -->
						<div class="receipt-left">
							<div class="receipt">
								<div class="receipt-header">
									<h2>注文内容</h2>
									<div class="customer-id">
										ID:
										<c:out value="${vm.customer.customerId}" default="N/A" />
									</div>
								</div>

								<c:set var="grandTotal" value="0" />
								<c:forEach items="${vm.orders}" var="order" varStatus="status">
									<c:set var="menu" value="${vm.menuMap[order.menuId]}" />
									<c:set var="orderTotal"
										value="${vm.orderTotalPrices[status.index]}" />
									<c:set var="grandTotal" value="${grandTotal + orderTotal}" />
									<div class="order-item">
										<div class="item-details">
											<div class="menu-line">
												<span><c:out value="${menu.name}" default="N/A" /></span> <span
													class="price">¥<fmt:formatNumber
														value="${menu.price}" pattern="#,###,##0" /></span>
											</div>
											<c:if test="${not empty order.optionIds}">
												<c:forEach items="${order.optionIds}" var="optionId">
													<c:set var="option" value="${vm.optionMap[optionId]}" />
													<c:if test="${not empty option}">
														<div class="option-line">
															<span>+ <c:out value="${option.name}" /></span> <span
																class="price">¥<fmt:formatNumber
																	value="${option.price}" pattern="#,###,##0" /></span>
														</div>
													</c:if>
												</c:forEach>
											</c:if>
										</div>
										<div class="item-total status-${order.statusId}">
											<div class="item-total-left">
												<c:if test="${not empty order.statusName}">
													<span class="status-badge status-${order.statusId}">
														<c:out value="${order.statusName}" />
													</span>
												</c:if>
											</div>
											<div class="item-total-right">
												<span>小計</span> <span class="price">¥<fmt:formatNumber
														value="${orderTotal}" pattern="#,###,##0" /></span>
											</div>
										</div>
									</div>
								</c:forEach>
							</div>
						</div>

						<!-- 提供済み以外のステータスがあるかをチェック -->
						<c:set var="hasUnservedOrders" value="false" />
						<c:forEach items="${vm.orders}" var="order">
							<c:if test="${order.statusId != 3}">
								<c:set var="hasUnservedOrders" value="true" />
							</c:if>
						</c:forEach>

						<!-- Right Column: Summary and Actions -->
						<div class="receipt-right">
							<div class="summary-panel">
								<c:if test="${not empty message}">
									<div class="error-message">⚠️ ${message}</div>
								</c:if>
								<div class="summary-top">
									<div class="summary-header">
										<h3>📊 会計情報</h3>
									</div>

									<div class="order-count">
										<span>注文数</span> <span class="count-value">${vm.orders.size()}
											品</span>
									</div>

									<div class="customer-count">
										<span>人数</span> <span class="count-value">${vm.customer.customerCount}
											名</span>
									</div>
								</div>

								<div class="summary-bottom">
									<div class="grand-total">
										<span>合計金額</span> <span class="price">¥<fmt:formatNumber
												value="${grandTotal}" pattern="#,###,##0" /></span>
									</div>

									<div class="action-buttons">
										<c:choose>
											<c:when test="${hasUnservedOrders}">
												<button type="button" 
													class="complete-btn warning-btn"
													onclick="completeCheckout(true)">✓ 会計完了</button>
											</c:when>
											<c:otherwise>
												<button type="button" 
													class="complete-btn"
													onclick="completeCheckout(false)">✓ 会計完了</button>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</div>
					</div>

					<!-- Hidden current customerId for JS -->
					<input type="hidden" id="currentCustomerId"
						value="${vm.customer.customerId}" />

				</c:otherwise>
			</c:choose>
		</div>
	</div>

	<!-- Error Modal -->
	<div id="errorModal" class="modal">
		<div class="modal-content error-modal">
			<div class="modal-header error-header">
				<h3>エラー</h3>
			</div>
			<div class="modal-body">
				<p id="errorModalMessage"></p>
			</div>
			<div class="modal-footer">
				<button type="button" class="modal-btn modal-btn-confirm"
					onclick="closeErrorModal()">OK</button>
			</div>
		</div>
	</div>

	<!-- Confirmation Modal -->
	<div id="confirmModal" class="modal">
		<div class="modal-content">
			<div class="modal-header">
				<h3 id="modalTitle">確認</h3>
			</div>
			<div class="modal-body">
				<p id="modalMessage"></p>
			</div>
			<div class="modal-footer">
				<button type="button" class="modal-btn modal-btn-cancel"
					onclick="closeModal()">キャンセル</button>
				<button type="button" class="modal-btn modal-btn-confirm"
					id="modalConfirmBtn">OK</button>
			</div>
		</div>
	</div>

	<!-- Success Modal -->
	<div id="successModal" class="modal">
		<div class="modal-content success-modal">
			<div class="modal-header success-header">
				<div class="success-icon">✓</div>
				<h3>会計完了</h3>
			</div>
			<div class="modal-body">
				<p>会計が完了しました。</p>
			</div>
			<div class="modal-footer">
				<button type="button" class="modal-btn modal-btn-confirm"
					onclick="closeSuccessModal()">OK</button>
			</div>
		</div>
	</div>

	<script src="${pageContext.request.contextPath}/assets/js/checkout.js"></script>
</body>
</html>
