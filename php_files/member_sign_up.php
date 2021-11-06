<?php
header('content-type: text/html; charset=utf-8');
// 데이터베이스 접속 문자열. (db위치, 유저 이름, 비밀번호)
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("FlyTogether",$connect);
//db연결 실패시
  if (!$connect) {
      echo "연결실패";
      die('Could not connect: ' . mysql_error());

}
  session_start();
  $id = $_POST[u_id];
  $pw = $_POST[u_pw];
  $name = $_POST[u_name];


  $sql = "INSERT INTO USER_INFO (USER_ID, USER_NAME, USER_PW) VALUES('$id', '$name', '$pw')";
  $result = mysql_query($sql, $connect) or die(mysql_error($connect));

  //성공이면 1을 리턴한다.
  echo $result;
 mysql_close($connect);


//회원가입이라면
//끝

?>
