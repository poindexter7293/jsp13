package sec01.ex01;



//주제 : Statement 를  PreparedStatement 로 업그레이드 


/*
	DAO 클래스 역할
	- 오라클 DBMS 서버에 만들어져 있는 t_member테이블과 연결 하여 
	  데이터베이스 작업(비즈니스로직 처리) - SELECT, INSERT, DELETE, UPDATE 을 하는 클래스.
*/

import java.sql.*;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class MemberDAO {	// 오라클 DBMS서버의 XE 데이터베이스 내부에 만들어 놓은 t_member테이블과 연결해서 DB작업 하는 클래스.

	//DB 와 연결해서 작업할 삼총사 객체들을 저장할 참조변수들 선언 	
	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet rs;
	
	//DataSource(커넥션풀) 객체의 주소를 저장할 변수 선언
	private DataSource dataSource;
	
	//MemberDAO 생성자 
	//-> 이 생성자는 new MemberDAO();를 호출하면 자동으로 실행!
	//-> 목적 : 톰캣서버(context.xml)에 등록된 "DB 연결 정보"를 찾아서, 
	//         그안에 있는 "DataSource"커넥션 풀 객체를 얻어 초기화 하는 작업
	public MemberDAO() {		
		try {
			//순서1. InitialContext 객체 생성
			//-> 이 객체를 통해 우리는 톰캣서버의 context.xml에 설정된 커넥션풀 자원 <Resource>를 찾을수 있게 됩니다.
			//-> 톰캣 서버 내부에 등록된 <Resource>태그의 자원을 검색할수 있게 도와주는 객체.
			Context ctx = new InitialContext();
			
			//순서2. "톰캣 서버 내부 환경" 에 접근
			//-> lookup("java:/comp/env")  이경로는 톰캣 서버에서 설정된 환경 변수들이 모여있는 경로로 
			//   context.xml 파일에 등록된 DB 정보들이 이안에 위치하기 떄문에 이경로를 저장 해 준다.
			Context envCtx = (Context)ctx.lookup("java:/comp/env");
			
			//순서3. jdbc/oracle 이라는 JNDI이름을 이용하여 
			//      context.xml 설정파일에 설정 해 놓은 <Resource>태그의 DataSource객체를 만들어 불러옵니다.
			dataSource = (DataSource)envCtx.lookup("jdbc/oracle");
	        // ────────────── 📌 텍스트 메모리 모델링 ③ ──────────────
	        // [context.xml 내부에 이런 설정이 있어야 함]
	        /*
	            <Resource name="jdbc/oracle"
	                      type="javax.sql.DataSource"
	                      driverClassName="oracle.jdbc.OracleDriver"
	                      url="jdbc:oracle:thin:@localhost:1521:xe"
	                      username="scott"
	                      password="tiger"
	                      maxActive="50"
	                      maxIdle="10"
	                      maxWait="-1"/>
	        */
	        // 정리: 이제부터는 dataSource.getConnection()을 호출하면
	        //         실제 DB에 접속된 Connection을 꺼내서 사용할 수 있음!
			
			
		}catch(Exception e) {		
			//context.xml에 작성한 DataSource커넥션 객체를 얻지 못할때 발생할 예외메세지 출력
			System.out.println("DataSource 커넥션풀 객체 얻기 실패 : " + e.toString());
		}
		
	}
	//isExisted 메소드 정의
	// - 입력한 아이디와 비밀번호가 저장된 MemberVo객체 주소 하나를 매개변수로 전달받아
	//   데이터베이스의 t_member테이블에 저장되어 있는지 없는지 조회 하여  그결과를 확인 시켜 주는 기능의 메소드
	public boolean isExisted(MemberVO  memberVO) {
		
		//입력한 아이디,비밀번호가 t_member테이블에서 조회되면? true 저장, 조회되지 않으면 false를 저장할 변수 선언
		boolean result = false;
		
		try {
			//순서1. 커넥션풀(DataSource)공간에서 미리 데이터베이스와 연결 해 놓은 Connection객체 하나 빌려오기
			//요약 : MemberDAO.java와 데이터베이스 연결
			con = dataSource.getConnection();
			
			//순서2. select (SQL) 문 만들기
			//-> 입력한 아이디 비밀번호를 갖는 회원 레코드 한쌍을 조회 하는데...
			//   조회된 회원 레코드 갯수가 1이면 'true' 조회 결과가 나오게 하고
			//   조회된 회원 레코드 갯수가 1이 아닌 0이면  'false' 문자열로 조회 결과가 출력되게 하는 select 문 
			String query = "select  decode( count(*), 1, 'true', 'false' ) as result from t_member "
						 + "where id=? and pwd=?";
			
			//순서3. query 변수에 저장된 전체 select 문장의 문자열을 미리 로드한 PreparedStatement실행 객체 얻기
			pstmt = con.prepareStatement(query);
			
			//순서3.1. PreparedStatement 실행 객체 메모리에 미리 로드된 전체 select 문 중에서 ? 기호들에 설정될 값들을 우리가 입력한 아이디, 비밀번호로 바꿔 설정
			pstmt.setString(1, memberVO.getId()); //첫번째 ? 기호 대신  입력한 아이디로 설정
			pstmt.setString(2, memberVO.getPwd());//두번쨰 ? 기호 대신  입력한 비밀번호 설정
			/*
			 select  decode( count(*), 1, 'true', 'false' ) as result from t_member
			 where id='kim' and pwd='1212'
			*/		
			
			//순서4. PreparedStatement 실행 객체 메모리에 저장후 완성된 위 select 구문을 데이터베이스의 t_member테이블에 전달해서 실행합니다.
			//      조회된 결과 데이터를 ReulstSet 임시 객체 메모리에 담아 ReulstSet 임시 객체 메모리 자체를 얻자
			rs = pstmt.executeQuery();
			/*
			 ResultSet 객체 메모리 모습
			 ---------------------
			 
			 커서 ->  RESULT
			        true
			 */
			
			//순서5. ResultSet 임시 객체 메모리의 커서(화살표)의 위치를 조회된 레코드 행의 위치로 내려 주면서 조회된 레코드가 존재하는지 유무 판단
			/*
			 ResultSet 객체 메모리 모습
			 ---------------------
			 
			  		RESULT
			  커서 ->  true
			 */	
			if(rs.next()) {
	
				//조회된  'true' 문자열 얻어  위 boolean result; 변수에 저장하기 위해 Boolean클래스의 parseBoolean메소드를 사용하여  'true'-> true 로 변환
				result = Boolean.parseBoolean(rs.getString("RESULT"));
				//       Boolean.parseBoolean('true'); -> true				
			}
				
		} catch (Exception e) {
			System.out.println("MemberDAO의 isExited 메소드 내부에서 select SQL문 실행 오류 : " + e);
		} finally {
			//순서6. 위 Connection객체 DataSource 컨넥션 풀로 반납,  PreparedStatement 실행 객체 제거,  ResultSet 임시 객체 제거
			ResourceClose();
		}
		
		return result;//순서7. 로그인 요청을 위해 입력한 아이디, 비밀번호에 해당하는 회원 레코드가 조회 되면? true 반환 , 조회 되지 않으면 false 반환 
	}
	
	
	
	
	//modMember 메소드 정의
	//- 수정할 회원아이디를 매개변수로 전달 받아 회원 조회 하는 기능의 메소드.
	public MemberVO modMember(String modId) {
		
		MemberVO vo = null;
		
		try {
			//순서1. 커넥션 풀 공간에서 커넥션 객체 하나 얻기 
			//요약 : DB 연결
			con = dataSource.getConnection();
			
			//순서2. 위 String id 매개변수로 전달 받은 삭제할 회원 아이디에 해당하는 회원레코드(행데이터) 삭제시키는 SELECT 문 작성
			//요약 : SQL문 만들기
			String query = "select * from t_member where id=?";
			
			//순서3. query변수에 저장된 전체 select 문자열을 미리 로드한 
			//      PreparedStatement SQL 실행 객체 얻기
			pstmt = con.prepareStatement(query);
			
			//순서3-1. PreapredStatement 실행 객체에 미리로드된  select 문자열 중에서  ? 기호 대신 String id매개변수로 받은 삭제할 회원 아이디 설정
			pstmt.setString(1, modId); //-> "select *  from t_member where id='hong'"
			
			//순서4. PreparedStatement 실행 객체에 완성된 select 전체 문장을  DB의 t_member테이블에 전송해 실행!
			rs = pstmt.executeQuery();
					
			//순서5. 조회된 회원 레코드들이 ResultSet임시 객체 메모리에 표형태로 저장되어 있으면 
			//회원 레코드(행)단위의 조회된 열(컬럼)값들을 차례대로 얻어
			//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
			if(rs.next()) {
				
				//커서가 위치한 조회된 회원 레코드(한 행의 데이터)의 열의 값들을 차례대로 얻어 변수에 저장
				String id = rs.getString("ID"); //"hong" 
				String pwd = rs.getString("PWD");//"1212" 
				String name = rs.getString("NAME");//"홍길동"
				String email = rs.getString("EMAIL");//"hong@gamil.com"
				Date   joinDate =  rs.getDate("JOINDATE"); // new Date("2026/01/28"); 
														   
				//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
				 vo = new MemberVO();
				 vo.setId(id);
				 vo.setPwd(pwd);
				 vo.setName(name);
				 vo.setEmail(email);
				 vo.setJoinDate(joinDate);
			}		
					
		} catch (Exception e) {
			System.out.println("MemberDAO의 delMember메소드 내부의 코드에서 delete문 실행 오류 : " + e);
			e.printStackTrace();
		} finally {
			//순서6. 사용한 메모리들(PreparedStatement객체 , Connection객체 ) 자원해제 
			ResourceClose();
		}
		
		//순서7. 수정할 회원 조회 후 반환 
		return vo;
	}
	
	
	//delMember메소드 정의
	//- 삭제 <a> 링크를 클릭했을때.. MemberServlet서블릿으로 전송요청한 삭제할 회원 아이디를 
	//  현재 보고 있는 delMember메소드의 매개변수 String id로 전달 받아
	//  delete SQL문을 완성한 후 ~~  DB의 t_member테이블에 저장된 하나의 회원레코드 정보 삭제 시키는 기능의 메소드 
	public void delMember(String id) {
		
		try {
			//순서1. 커넥션 풀 공간에서 커넥션 객체 하나 얻기 
			//요약 : DB 연결
			con = dataSource.getConnection();
			
			//순서2. 위 String id 매개변수로 전달 받은 삭제할 회원 아이디에 해당하는 회원레코드(행데이터) 삭제시키는 DELETE 문 작성
			//요약 : SQL문 만들기
			String query = "delete from t_member where id=?";
			
			//순서3. query변수에 저장된 전체 delete 문자열을 미리 로드한 
			//      PreparedStatement SQL 실행 객체 얻기
			pstmt = con.prepareStatement(query);
			
			//순서3-1. PreapredStatement 실행 객체에 미리로드된  delete 문자열 중에서  ? 기호 대신 String id매개변수로 받은 삭제할 회원 아이디 설정
			pstmt.setString(1, id); //-> "delete from t_member where id='hong'"
			
			//순서4. PreparedStatement 실행 객체에 완성된 delete 전체 문장을  DB의 t_member테이블에 전송해 실행!
			pstmt.executeUpdate();
			
			//참고  executeUpdate(); <-- INSERT, UPDATE, DELETE 구문 실행시 사용
			//                      <-- SQL문 실행시 성공하면 성공한 레코드 갯수 1반환 실패하면 0반환
			
			//     executeQuery();  <-- SELECT 구문 실행시 사용
			//						<-- SQL문 실행시 조회된 결과 데이터들을 ReusltSet임시메모리객체에 담아 반환			
			
		} catch (Exception e) {
			System.out.println("MemberDAO의 delMember메소드 내부의 코드에서 delete문 실행 오류 : " + e);
			e.printStackTrace();
		} finally {
			//순서5. 사용한 메모리들(PreparedStatement객체 , Connection객체 ) 자원해제 
			ResourceClose();
		}
	
	}
	
	
	//addMember메소드 정의
	//- memberForm.html화면에서 입력한 가입할 새회원 데이터들을~~
	//  MemberVO객체를 생성해서 각 인스턴스변수에 저장한 뒤~~
	//  member.jsp내부에서  addMember메소드 호출시~ 매개변수 MemberVO vo로  전달받아 INSERT SQL문을 만들고
	// 만든 INSERT SQL문을 DB의 t_member테이블에 전송해서 새회원 정보를 추가 시키는 기능을 하는 메소드.
	//요약 : 입력한 새회원 정보를 DB의 테이블에 추가 
	public int addMember(MemberVO vo) {
		
		//회원가입에 성공하면 1을 저장시키고, 실패하면 0을 저장시킬 변수 선언
		int result = 0;
		
		try {
			//순서1. 커넥션풀(DataSource)에 미리 DB와 연결을 맺은 T4CConnection객체 빌려오기
			//요약 : DB연결
			con = dataSource.getConnection();
			
			//순서2. SQL문(insert문) 만들기 
			//방법 : 매개변수 MemberVO vo로 전달 받는 MemberVO객체의 인스턴스변수값들을 모두얻어 insert문장을 완성 시킨다.
			String id = vo.getId(); //입력한 아이디
			String pwd = vo.getPwd(); //입력한 비밀번호
			String name = vo.getName(); //입력한 이름 
			String email = vo.getEmail();//입력한 이메일 
			
			//순서2-1. insert 문장 만들기  version 1  :  Statement 실행 객체를 사용할 경우 만드는  insert 문장
			//String query = "insert into t_member(id, pwd, name, email)" + 
			///			   	"values('" + id + "','" + pwd + "','" + name + "','" + email + "')";
			
			//순서2-1. insert 문장 만들기 version 2 : PreparedStatement 실행 객체를 사용할 경우 만드는 insert 문장
			String query = "insert into t_member(id, pwd, name, email)" +
						   				 "values( ?,   ?,    ?,     ?)";
			
			//순서3. query변수에 저장된    "insert into t_member(id, pwd, name, email) values( ?,   ?,    ?,     ?)"
			//      미리 올려놓고 동적으로 insert문장을 완성 시킬 PreparedStatement실행 객체 얻기
			pstmt = con.prepareStatement(query);
			//-----------------------------------
			//PreaparedStaement 실행 객체 메모리에 insert문장이 저장된 모습
			//->  "insert into t_member(id, pwd, name, email) values( ?,   ?,    ?,     ?)"
			
			//순서3.1. PreaparedStatement 실행 객체 메모리에 insert문장의 ? 기호 대신  입력한 가입할 데이터들로 설정!
			//요약 : ? 설정
			//방법 ->  setter메소드를 호출해서 설정
			pstmt.setString(1, id); //첫번째 ?  대신   id변수에 저장된 입력한 아이디로 설정
			pstmt.setString(2, pwd); //두번째 ? 대신 pwd변수에 저장된 입력한 비밀번호로 설정  
			pstmt.setString(3, name);//세번쨰 ? 대신 name변수에 저장된 입력한 이름을 설정 
			pstmt.setString(4, email);//네번째 ? 대신 email변수에 저장된 입력한 이메일로 설정 
			
			//PreaparedStaement 실행 객체 메모리에 insert문장이 저장된 모습
			//->  "insert into t_member(id, pwd, name, email) values('admin', '1234', '홍길동2', 'admin@naver.com')"
			
			//순서4. PreaparedStaement 실행 객체 메모리에 완성된 전체 insert 문장을 DB의 t_member테이블에 전송해서 실행!
			result = pstmt.executeUpdate(); //insert 문장 실행에 성공하면 insert에 성공한 레코드 갯수 1반환
								   		    //insert 문장 실행에 실패하면 0을 반환
						
		} catch (Exception e) {
			System.out.println("MemberDAO클래스의 addMember메소드 내부의 코드에서 insert SQL문 실행 오류 : " + e);
			e.printStackTrace();
		} finally {
			//순서5. 위 DB관련 작업할 객체 메모리들 모두 사용이 끝나면 메모리 낭비 이므로 자원 제거
			ResourceClose();
		}
		
		//순서6. 새회원 추가(회원가입 성공) 1  또는    (실패) 0을  member.jsp으로 반환  
		return result;
	}
	
	
	//listMembers 메소드 정의
	//- 오라클 DBMS 서버 내부의 XE데이터베이스에 만들어진 t_member테이블에 저장된 모든 회원 레코드들을 한번에 조회 해서 가져와 
	//  레코드(행) 단위로 MemberVO클래스의 객체에 저장 후   
	//  MemberVO객체 들을  ArrayList 배열에 추가 해서 저장 후  조회된 회원정보들이 저장된 ArrayList 배열 자체를 반환 하는 메소드 
	public ArrayList listMembers() {
		
		//t_member테이블에 저장된 모든 회원 레코드들을 조회 해서 가져와 
		//가변길이 배열의 각 index위치 칸에 임시로 저장할 배열공간인? ArrayList 배열 생성 
		ArrayList  list = new ArrayList();
		
		try {
			//DataSource(커넥션풀)공간에서  미리 DB의 테이블과 연결을 맺어 놓은 T4CConnection 접속 객체하나 빌려오기 
			//이유 : DB와 연결해서 작업하기 위함
			con = dataSource.getConnection();
			
			//순서5. SQL문장 (SELECT 문장) 작성
			//-> t_member 테이블에 저장된 모든 회원 레코드들을 조회하는 SQL문장 작성
			String query = "select * from t_member";
			
			//순서5.1. T4CConnection객체의 prepareStatement메소드 호출시~~~ 매개변수로 순서5.에서 미리 준비한 select * from t_member 문장을 전달하면
			//        OraclePreparedStatementWrapper 실행 객체 메모리에  select * from t_member 문장 전체를 저장 한 후 
			//         OraclePreparedStatementWrapper 실행 객체 주소 자체를 반환 해 줍니다.
			pstmt = con.prepareStatement(query);
			/*
			OraclePreparedStatementWrapper 실행 객체 메모리 안에 저장된 모습
			----------------------------------------------
			select * from t_member
			-----------------------------------------------	
			*/
					
			//순서6. SQL문장을  오라클 DBMS 서버의 XE 데이터베이스의 t_member테이블에 전송(전달)해서 실행!(조회)
			//"select * from t_member" SQL문을 이용하여 조회 후 조회한 결과 데이터들을 ResultSet객체 메모리에 저장후 반환 받습니다.
			//단! 조회된 화면의 커서(화살표) 위치는 가장 처음에 조회된 표형태의 제목열 행 가리키고 있다.
			rs = pstmt.executeQuery(); //<- ResultSet 객체 메모리 반환 
			
			
			//순서7. 조회된 회원 레코드들이 ResultSet임시 객체 메모리에 표형태로 저장되어 있으면 계속반복해서
			//회원 레코드(행)단위의 조회된 열(컬럼)값들을 차례대로 얻어
			//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
			//마지막으로 생성된 MemberVO객체들을 차례대로 ArrayList배열에 반복해서 추가시킵니다.
			while(rs.next()) {
				
				//커서가 위치한 조회된 회원 레코드(한 행의 데이터)의 열의 값들을 차례대로 얻어 변수에 저장
				String id = rs.getString("ID"); //"hong" , "lee",    "kim"
				String pwd = rs.getString("PWD");//"1212",  "1212",  "1212"
				String name = rs.getString("NAME");//"홍길동", "이순신",  "김유신"
				String email = rs.getString("EMAIL");//"hong@gamil.com", "lee@test.com", "kim@web.com"
				Date   joinDate =  rs.getDate("JOINDATE"); // new Date("2026/01/27"); 
														   // new Date("2026/01/27");
														   // new Date("2026/01/27");
										
				
				//MemberVO객체를 행단위로 생성하여 각 인스턴스변수에 저장시킵니다.
				MemberVO vo = new MemberVO();
						 vo.setId(id);
						 vo.setPwd(pwd);
						 vo.setName(name);
						 vo.setEmail(email);
						 vo.setJoinDate(joinDate);
						 
				//마지막으로 생성된 MemberVO객체를 차례대로 ArrayList배열에 반복해서 추가시킵니다.
				list.add(vo);
				
				//ArrayList가변길이 배열 모습
				//[ MemberVO,  MemberVO,  MemberVO ]
				//    0          1            2       index	
			}		
					
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			//순서9. DB작업 관련 객체 메모리를 모두 사용하고 난 다음  필요 없으면 메모리 낭비 이므로 메모리 톰캣에서 제거 
			ResourceClose();
		}
		
		return  list; //ArrayList 배열 메모리 자체를 member.jsp으로 반환
	}
	
	//DB 작업 관련 객체 메모리들 사용이 끝난 후 자원 해제 하는 기능의 메소드
	public void ResourceClose() {		
		try {
			//ResultSet객체는 SQL문(SELECT)의 조회된 결과데이터를 임시로 저장하는 객체 입니다.
			//이 객체를 사용하고 난다음에  제거시키자.
			if(rs != null) rs.close();
			
			//PreparedStatement객체는 SQL문(SELECT)를 실행하는 객체 입니다.
			//이 객체를 사용하고 난 다음에 제거 시키자.
			if(pstmt != null) pstmt.close();
			
			//T4CConnection객체는  데이터베이스와의 연결을 관리하는 객체로, 더이상 데이터베이스 연결할 필요가 없으면 
			//이객체를 사용하고 난 다음에 제거 시키자.
			if(con != null) con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	

	
}// class MemberDAO  











