package com.chatsoone.rechat.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatsoone.rechat.data.entity.Folder

@Dao//폴더의 정보
interface FolderDao {
    //폴더 추가, 검증된
    @Insert
    fun insert(folder: Folder)

    // 폴더가 있는지 없는지 검사하기 위해
    @Query("SELECT COUNT(*) FROM FolderTable WHERE kakaoUserIdx = :kakaoUserIdx")
    fun getFolderCount(kakaoUserIdx: Long): Int

    //모든 폴더목록 조회, 검증된
    @Query("SELECT * " +
            "FROM FolderTable\n" +
            "WHERE kakaoUserIdx = :userIdx AND status != 'HIDDEN';")
    fun getFolderList(userIdx: Long): LiveData<List<Folder>>//liveData?

    //숨긴 폴더 목록 조회, 검증된
    @Query("SELECT *\n" +
            "          FROM FolderTable\n" +
            "          WHERE kakaoUserIdx = :userIdx AND status = 'HIDDEN';")
    fun getHiddenFolder(userIdx:Long): LiveData<List<Folder>>

    //폴더 이름 바꾸기, 검증된
    @Query("UPDATE FolderTable\n" +
            "          SET folderName = :folderName\n" +
            "          WHERE Idx = :folderIdx;")
    fun updateFolderName(folderIdx: Int, folderName: String)

    //폴더 아이콘 바꾸기, 검증된
    @Query("UPDATE FolderTable\n" +
            "          SET folderImg = :folderIcon\n" +
            "          WHERE idx = :folderIdx;")
    fun updateFolderIcon(folderIdx: Int, folderIcon:Int)

    //폴더 숨기기, 검증된
    @Query("UPDATE FolderTable\n" +
            "        SET status = 'HIDDEN'\n" +
            "        WHERE idx = :idx;")
    fun updateFolderHide(idx: Int)

    //폴더 숨김 해제하기, 검증된
    @Query("UPDATE FolderTable\n" +
            "        SET status = 'ACTIVE'\n" +
            "        WHERE idx = :idx;")
    fun updateFolderUnHide(idx: Int)

    //폴더 삭제(폴더 삭제시 순서대로 실행), 검증된
    @Query("DELETE FROM FolderContentTable WHERE folderIdx = :folderIdx")
    fun deleteFolderContent(folderIdx:Int)

    @Query("DELETE FROM FolderTable\n" +
            "       WHERE idx = :idx;")
    fun deleteFolder(idx: Int)
}