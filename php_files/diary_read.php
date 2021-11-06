<?php
session_start();
$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
// 데이터베이스 선택
mysql_select_db("FlyTogether",$connect);
if($connect){
  //echo "db 연결 했슈";
}
if (!$connect) {
   echo "연결실패";
   die('Could not connect: ' . mysql_error());
}

$num = $_POST[board_id];

$sql = "select title, content, emotion, img_dir from diary where diary_index=".$num;
$result = mysql_query($sql, $connect);
   if(!$result){
   die('Could not query:' . mysql_error());
     echo "fail";
     echo die('Could not query:' . mysql_error());
 }

$row = mysql_fetch_assoc($result);
  $title = $row['title'];
  $time = $row['emotion'];
  $image = $row['img_dir'];
  $content = $row['content'];
  $json_object = array('id'=>$id, 'title'=>$title, 'time'=>$time, 'writer'=>$writer, 'image'=>$image, 'content' => $content);
  echo json_encode($json_object);
mysql_close($connect);
 ?>
