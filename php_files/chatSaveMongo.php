<?php
try {

    $vodtitle = $_POST[vod_name];
    $userid = $_POST[u_id];
    $chatmsg = $_POST[chat_msg];
    $chattime = $_POST[chat_time];

    $mng = new MongoDB\Driver\Manager("mongodb://localhost:27017");
        $bulk = new MongoDB\Driver\BulkWrite;

    $doc = ['_id' => new MongoDB\BSON\ObjectID, 'vodtitle' => $vodtitle, 'userid' => $userid, 'chatmsg' => $chatmsg, 'chattime' => $chattime];
    $bulk->insert($doc);

    //update 쿼리
    //$bulk->update(['name' => 'Audi'], ['$set' => ['price' => 52000]]);
    //delete 쿼리
    //$bulk->delete(['name' => 'Hummer']);

    $mng->executeBulkWrite('testdb.chat', $bulk);
    echo "1";

} catch (MongoDB\Driver\Exception\Exception $e) {

    $filename = basename(__FILE__);

    echo "The $filename script has experienced an error.\n";
    echo "It failed with the following exception:\n";

    echo "Exception:", $e->getMessage(), "\n";
    echo "In file:", $e->getFile(), "\n";
    echo "On line:", $e->getLine(), "\n";
}

?>
