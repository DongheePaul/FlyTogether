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

 $sql = "SELECT  * FROM diary";
  $result = mysql_query($sql, $connect) or die(mysql_error($connect));

  $total_rows = mysql_num_rows($result);
//select 쿼리 실패 시
if(!$result){
  die('vod_table select failed :'.mysql_error());
  //select 쿼리 성공 시
}else{
    $vod_id[] = array();
    $bj_id[] = array();
    $vod_title[] = array();
    $vod_thumbnail[] = array();
    $array_result = array();

    while($row = mysql_fetch_assoc($result)){
    $vod_id = $row['diary_index'];
    $bj_id= $row['user_id'];
    $vod_title = $row['title'];
    $vod_thumbnail = $row['img_dir'];

    array_push($array_result, array(
      'vod_id'=>$vod_id,
      'bj_id' => $bj_id,
      'vod_title'=>$vod_title,
      'vod_thumbnail'=>$vod_thumbnail
    ));
    }

    echo json_encode($array_result);
    }


 mysql_close($connect);

?>
