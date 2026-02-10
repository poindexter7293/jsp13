<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
		include2.jsp의 상단 영역 코드 입니다.
		<br>
			
<%--  <jsp:include page="재요청해서 실행할 코드가 작성된 서버페이지 주소 경로"> --%>
		
	  <jsp:include page="duke_image.jsp" >	
	  
			<jsp:param name="name"   value="duke2"/>
			<jsp:param name="imgName" value="duke2.png"/>
		
	  </jsp:include>	

		<br>
		include2.jsp의 하단 영역 코드 입니다.
		
</body>
</html>
