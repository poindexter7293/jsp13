<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
include1.jsp의 상단 영역 코드 입니다.

<jsp:include page="duke_image.jsp">

	<jsp:param name="name" value="duke"/>
	<jsp:param name="imgName" value="duke.png"/>
	
</jsp:include>


include1.jsp의 하단 영역 코드 입니다.
</body>
</html>