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

  $sql = "SELECT IF(strcmp(USER_PW,'$pw'),0,1) pw_chk FROM USER_INFO  WHERE USER_ID = '$id'";
  $result = mysql_query($sql, $connect);

  // 쿼리 결과
  if($result)  {
    $row = mysql_fetch_array($result);
    //아이디가 존재하지 않는다면
    if(is_null($row[pw_chk]))
    {
      $output = array();
      $output["result"] = "null";
      $output["name"] = $name;
         $output =  json_encode($output);
         echo  $output;
    }
    //비밀번호 일치
    else if($row[pw_chk] == 1){
      $sql = "select USER_ID, USER_NAME from USER_INFO where USER_ID='$id'";
      $result = mysql_query($sql,$connect);
      $row = mysql_fetch_assoc($result);

      $output = array();
      $output["result"] = "1";
      $output["id"] = $id;
      $output["name"] = $row['USER_NAME'];
      $output = json_encode($output);
         echo  $output;
         mysql_close();
    }
    //비밀번호 불일치
    else if($row[pw_chk] == 0){
      $output = array();
      $output["result"] = "0";
      $output["id"] = $id;
      $output["name"] = $name;
         $output =  json_encode($output);
         echo  $output;
    }
  }
  //에러 발생시
  else
  {
   echo "mysql_errno($connect)";
  }

?>
