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
  $room_name = $_POST[room_name];

  $sql = "INSERT INTO streaming_room (BJ_ID, ROOM_NAME) VALUES('$id', '$room_name')";
  $result = mysql_query($sql, $connect) or die(mysql_error($connect));

  if($result == 1){
    $sql1 = "SELECT MAX(ROOM_INDEX) FROM streaming_room WHERE BJ_ID = '$id'";
    $result = mysql_query($sql1,$connect);
    $row = mysql_fetch_assoc($result);

    $output = array();
    $output["result"] = "1";
    $output["id"] = $row['MAX(ROOM_INDEX)'];
    $output = json_encode($output);
       echo  $output;
  }
 mysql_close($connect);



?>
