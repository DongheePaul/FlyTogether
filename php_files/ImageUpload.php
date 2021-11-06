<?php
session_start();
$data = $_POST["data1"]; //FlytogetherImage라는 값이 넘어옴
$file_path = $data."/"; // FlytogetherImage 밑을 의미하므로 /를 붙여줌
echo "data=>".$data."////";
if(is_dir($data)){
	echo "폴더 존재 O"; // pass
} else {
	echo "폴더 존재 X";
	echo $data;
	@mkdir($data, 0777);
	@chmod($data, 0777);
}

	// basename : 디렉토리명이 있다면, 그 부분을 제외하고 파일명만 출력,
	// 즉 abc/def/ghi.jpg 면 ghi.jpg만 가져올 수 있음
	$file_name = basename( $_FILES['uploaded_file']['name']);
	echo "file_name==>".$file_name."////";
	$file_path = $file_path . $file_name;

	$connect=mysql_connect("222.239.249.149", "root", ")0p9o8i7u");
	// 데이터베이스 선택
	mysql_select_db("gazuaa",$connect);
	if($connect){
	  //echo "db 연결 했슈";
	}
	if (!$connect) {
	   echo "연결실패";
	   die('Could not connect: ' . mysql_error());
	}


	if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
		echo "file upload success";
		echo "///".$file_path;
		// $sql = "UPDATE users SET profile_image='$file_path' WHERE email='$user_id'";
		// $result = mysql_query($sql, $connect);
		// if(!$result){
		// 	die('Could not query:' . mysql_error());
		// }
		// else{
		// 	echo "db에 경로 삽입";
		// }
	}
	 else{
		if($_FILES['uploaded_file']['error'] > 0){
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
		echo "file upload fail".$_FILES['uploaded_file']['error'];
	}
	mysql_close($connect);
?>
