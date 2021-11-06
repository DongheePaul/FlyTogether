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
  $sql = "DELETE FROM streaming_room WHERE BJ_ID ='$id'";
  $result = mysql_query($sql, $connect) or die(mysql_error($connect));


       echo  $result;

 mysql_close($connect);



?>
