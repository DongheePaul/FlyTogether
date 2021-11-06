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

  $sql = "SELECT  * FROM streaming_room";
  $result = mysql_query($sql, $connect);

    $total_rows = mysql_num_rows($result);

  // 쿼리 결과
  if($result)  {
    $roomid[] = array();
    $bj_id[] = array();
    $room_name[] = array();
    $array_result = array();
  while($row = mysql_fetch_assoc($result)){
    $room_index = $row['ROOM_INDEX'];
    $bjid= $row['BJ_ID'];
    $roomname = $row['ROOM_NAME'];
    array_push($array_result, array(
      'room_index'=>$room_index,
      'bj_id' => $bjid,
      'room_name'=>$roomname));
  }
  echo json_encode($array_result);
}
  //에러 발생시
  else
  {
   echo "mysql_errno($connect)";
  }

?>
