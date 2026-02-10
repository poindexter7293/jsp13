
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="sec01.ex01.MemberDAO"%>
<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<% request.setCharacterEncoding("utf-8");	//1. 요청한 데이터 request내장객체에 저장된 한글문자 인코딩 방식 UTF-8설정 %>
<%--
	  2. 요청한 값들을 얻기 (회원가입을 위해 입력한 값들을 request객체 얻기)
	  3. MemberVO객체를 생성해서 각 인스턴스변수에 요청한 값들을 저장	  
 --%>
	<jsp:useBean id="vo" class="sec01.ex01.MemberVO"  scope="page"/>	
	<jsp:setProperty name="vo" property="*"    />

	<%-- 4. 위3.3.에서 조회한 t_member테이블의 정보를 클라이언트의 웹브라우저로 보여주자(응답해 주자) --%>
	
	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>		
		</tr>
	 	<!-- memberForm.html 화면에 가입을 위해 입력한 데이터들을 jsp:setProperty 태그로 저장시킨 MemberVO객체의 인스턴변수 값을  jsp:getProperty액션태그를 사용하여 얻어 출력 -->
		<tr align="center">
			<td width="7%"> <jsp:getProperty  name="vo" property="id"/> </td>  <!--  vo.getId(); 호출한 거와 같이 입력한 아이디를 반환해준다. -->
			<td width="7%"> <jsp:getProperty  name="vo" property="pwd"/> </td> <!--  vo.getPwd(); 호출한 거와 같이 입력한 비밀번호를  pwd변수의 값으로 반환해 준다. -->
			<td width="7%"> <jsp:getProperty  name="vo" property="name"/> </td> <!--  vo.getName();  -->
			<td width="7%"> <jsp:getProperty  name="vo" property="email"/> </td> <!--  vo.getEmail(); -->
		</tr>
	</table>
	
<%-- getProperty액션태그 사용
	 - 자바빈 역할을 하는 MemberVO객체의 private으로 은닉화된 인스턴스변수의 값을 반환시킬 getter메소드들을 
	     자바코드 대신 호출하는 태그.
	 - 자바코드의 for반복문이랑 같이 사용할수 없습니다. 
	   그러나 JSTL태그의 반복문문법과는 같이 사용할수 있다.
 	
 	 - 문법
 	 	<jsp:getProperty  name="useBean액션태그의 id속성에 작성한값"  
 	 	                  property="반환받을 값이 저장된 MemberVO의 인스턴스변수명"/>
 --%>














