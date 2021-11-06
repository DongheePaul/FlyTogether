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
//글쓴이
$writer = $_POST[user_id];
//게시글 제목
 $title = $_POST[title];
  //게시글 내용
 $content = $_POST[content];
$emotion = $_POST[emotion];

//게시판에 올릴 사진을 저장할 디렉토리명 : board_image
$file_path = "FlytogetherImage/";

$date = date("Y-m-d H:i:s");
$file_name = basename($_FILES['file']['name']);
echo $file_name."= file basename //";
$file_path = $file_path.$file_name;
echo $file_path."= file path //";

if(move_uploaded_file($_FILES['file']['tmp_name'], $file_path)){
$sql = "insert into diary (diary_index, user_id, title, content, emotion, img_dir, time) values ('0', '$writer', '$title','$content', '$emotion', '$file_path', '$date')";
$result = mysql_query($sql, $connect);

if(!$result){
  die('Could not query:' . mysql_error());
     echo "fail //";
    echo die('Could not query:' . mysql_error());
     	}
     	else{
     		echo "upload success";
     	}
} else{
 if($_FILES['file']['error'] > 0){
echo '{result: -1, ';
//오류 타입에 따라 echo 'msg: "오류종류"}';
switch ($_FILES['uploaded_file']['error']){
case 1: echo 'msg: "upload_max_filesize 초과"}';break;
case 2: echo 'msg: "max_file_size 초과"}';break;
case 3: echo 'msg: "파일이 부분만 업로드됐습니다."}';break;
case 4: echo 'msg: "파일을 선택해 주세요."}';break;
case 6: echo 'msg: "임시 폴더가 존재하지 않습니다."}';break;
case 7: echo 'msg: "임시 폴더에 파일을 쓸 수 없습니다. 퍼미션을 살펴 보세요."}';break;
case 8: echo 'msg: "확장에 의해 파일 업로드가 중지되었습니다."}';break;
 }
}
 echo "file upload fail. error_num =>".$_FILES['file']['error'];
}
mysql_close($connect);
?>
