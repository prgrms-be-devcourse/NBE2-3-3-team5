package com.example.festimo.domain.meet.dto

import com.example.festimo.domain.meet.entity.CompanionStatus

data class CompanionResponse(

    val title:String,  //모임 이름
    val companionId: Long,  // 모임 id
    val leaderId: Long,    // 리더 id
    val leaderName: String,  // 리더 이름
    val status: CompanionStatus,
    val members: List<MemberResponse>  // 동행원들
)
