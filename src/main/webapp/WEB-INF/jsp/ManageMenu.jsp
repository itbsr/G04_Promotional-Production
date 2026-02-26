<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8" />
		<title>メニュー管理</title>
		<link
			rel="stylesheet"
			type="text/css"
			href="<c:url value='/assets/css/ManageMenu.css'/>"
		/>
		<script src="<c:url value='/assets/js/ManageMenu.js'/>" defer></script>
	</head>

	<body>
		<h1>メニュー管理</h1>

		<button class="menu-add" onclick="openModal(event)">メニューを追加</button>
		<button class="menu-delete" onclick="deleteBtn(event)">メニューを削除</button>

		<div id="addMenuModal" class="modal">
			<div class="modal-content">
				<span class="close" onclick="closeModal()">&times;</span>
				<h2 id="modalTitle"></h2>
				<form id="addMenuForm" action="api/v1/menus" method="post">
					<input type="hidden" id="menuId" value="" />
					<div class="form-group">
						<label>商品名:</label>
						<input type="text" id="menuName" name="name" required />
					</div>
					<div class="form-group">
						<label>カテゴリ/サブカテゴリ:</label>
						<select id="categorySubSelect" name="categoryId" required>
							<option value="" data-category="">選択してください</option>
							<c:forEach var="category" items="${viewModel.categoryList}">
								<c:forEach var="sub" items="${category.children}">
									<option value="${sub.id}" data-category="${category.id}">
										${category.name} / ${sub.name}
									</option>
								</c:forEach>
							</c:forEach>
						</select>
					</div>
					<div class="form-group">
						<label>単価(円):</label>
						<input type="number" id="menuPrice" name="price" required min="0" max="99999999" />
					</div>
					<div class="form-group">
						<label>画像URL:</label>
						<select name="image" id="imageSelect" required>
							<option value="">-- 選択してください --</option>
							<c:forEach var="img" items="${viewModel.images}">
								<option value="menuImages/${img}">${img}</option>
							</c:forEach>
						</select>
					</div>
					<div class="form-button">
						<button type="button" class="optionModal" onclick="openOptionSelectModal()">オプションを選択</button>
						<!-- オプション選択モーダル -->
						<div id="optionSelectModal" class="modal">
							<div class="modal-content">
								<span class="close" onclick="closeOptionSelectModal()">&times;</span>
								<h2>オプション管理</h2>
								
								<!-- オプション登録・編集フォーム -->
								<div id="optionRegisterForm" class="option-register-form">
									<h3 id="optionFormTitle">新しいオプションを登録</h3>
									<input type="hidden" id="editOptionId" value="" />
									<div class="option-form-row">
										<label for="newOptionName">オプション名:</label>
										<input type="text" id="newOptionName" placeholder="例: 大盛り" required />
									</div>
									<div class="option-form-row">
										<label for="newOptionPrice">価格(円):</label>
										<input type="number" id="newOptionPrice" min="0" max="99999999" placeholder="例: 100" required />
									</div>
									<div class="option-form-buttons">
										<button type="button" id="registerOptionBtn" onclick="saveOption()">オプションを登録</button>
										<button type="button" id="cancelEditOptionBtn" onclick="cancelEditOption()" style="display: none;">キャンセル</button>
									</div>
								</div>
								
								<hr />
								
								<!-- オプション一覧 -->
								<h3>登録済みオプション一覧</h3>
								<div id="optionCell" class="option-cell">
									<!-- JavaScriptでオプション一覧を動的に生成 -->
									<div id="optionList"></div>
								</div>
								<div class="modal-buttons">
									<button type="button" onclick="closeOptionSelectModal()">閉じる</button>
								</div>
							</div>
						</div>
					</div>
					<div class="form-buttons">
						<button type="button" class="btn-cancel" onclick="closeModal()">閉じる</button>
						<button type="button" id="modalSubmitButton" class="btn-submit"></button>
					</div>
				</form>
			</div>
		</div>


		<table border="1">
			<tr>
				<th class="delete-checkbox-cell" style="display: none">削除</th>
				<th>商品名</th>
				<th>カテゴリ</th>
				<th>サブカテゴリ</th>
				<th>単価(円)</th>
				<th>削除済み</th>
			</tr>
			<c:forEach var="menu" items="${viewModel.manageMenuList}">
				<tr>
					<td class="delete-checkbox-cell" style="display: none">
						<div class="delete-menu">
							<input type="checkbox" name="deleteMenu" value="${menu.id}" />
						</div>
					</td>
					<td>
						<div
							class="menu-edit"
							data-menu-id="${menu.id}"
							data-menu-name="<c:out value='${menu.name}'/>"
							data-menu-category="<c:out value='${menu.categoryId}'/>"
							data-menu-price="<c:out value='${menu.price}'/>"
							data-menu-image-url="<c:out value='${menu.image}'/>"
							onclick="openModal(event)"
						>
							<c:out value="${menu.name}"/>
						</div>
					</td>

					<c:set var="categoryName" value="" />
					<c:forEach var="category" items="${viewModel.categoryList}">
						<c:forEach var="sub" items="${category.children}">
							<c:if test="${menu.categoryId == sub.id}">
								<c:set var="categoryName" value="${category.name}" />
							</c:if>
						</c:forEach>
					</c:forEach>
					<td>${categoryName}</td>
					
					<c:set var="subCategoryName" value="" />
					<c:forEach var="category" items="${viewModel.categoryList}">
						<c:forEach var="sub" items="${category.children}">
							<c:forEach var="sub" items="${category.children}">
								<c:if test="${menu.categoryId == sub.id}">
									<c:set var="subCategoryName" value="${sub.name}" />
								</c:if>
							</c:forEach>
						</c:forEach>
					</c:forEach>
					<td>${subCategoryName}</td>
					<td>${menu.price}</td>
					<td>
						<c:choose>
							<c:when test="${menu.deletedAt != null}">○</c:when>
							<c:otherwise></c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:forEach>
		</table>

		<div id="deleteControlPanel" style="display: none; margin-top: 20px">
			<p>
				選択中:
				<span id="selectedCount">0</span>
				件
			</p>
			<button id="executeDeleteBtn">削除を実行</button>
		</div>
	</body>
</html>
