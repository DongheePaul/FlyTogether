<?php
try {
  $name = $_POST[vod_name];
    $mng = new MongoDB\Driver\Manager("mongodb://localhost:27017");
    $filter = [ 'vodtitle' => $name ];
    $query = new MongoDB\Driver\Query($filter);
    $res = $mng->executeQuery("testdb.chat", $query);

    if(!empty($res)){
      $vodtitle[] = array();
      $userid[] = array();
      $chatmsg[] = array();
      $chattime[] = array();
      $array_result = array();

      foreach ($res as $row) {
        $vodtitle = $row->vodtitle;
        $userid= $row->userid;
        $chatmsg = $row->chatmsg;
        $chattime = $row->chattime;

            array_push($array_result, array(
              'vodtitle'=>$vodtitle,
              'userid' => $userid,
              'chatmsg'=>$chatmsg,
              'chattime'=>$chattime
            ));
      }

    echo json_encode($array_result);

    }else{
        echo "No match found\n";
    }

} catch (MongoDB\Driver\Exception\Exception $e) {

    $filename = basename(__FILE__);

    echo "The $filename script has experienced an error.\n";
    echo "It failed with the following exception:\n";

    echo "Exception:", $e->getMessage(), "\n";
    echo "In file:", $e->getFile(), "\n";
    echo "On line:", $e->getLine(), "\n";
}

?>
