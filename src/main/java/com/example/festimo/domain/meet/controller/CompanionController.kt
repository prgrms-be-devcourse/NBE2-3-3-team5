package com.example.festimo.domain.meet.controller

import com.example.festimo.domain.meet.dto.CompanionRequest
import com.example.festimo.domain.meet.dto.CompanionResponse
import com.example.festimo.domain.meet.service.CompanionService
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode.USER_NOT_FOUND
import com.example.festimo.global.utils.jwt.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/meet")
@Tag(name = "동행 API", description = "동행 관련 API")
class CompanionController(
    private val companionService: CompanionService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {
    private fun getEmailFromHeader(authorizationHeader: String): String {
        val token = authorizationHeader.replace("Bearer ", "")
        return jwtTokenProvider.getEmailFromToken(token)
    }

    private fun getUserFromEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw CustomException(USER_NOT_FOUND)
    }

    @PostMapping("/companions")
    @Operation(summary = "동행 생성")
    fun createCompanion(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody request: CompanionRequest
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        companionService.createCompanion(request.postId, email)
            ?: throw IllegalStateException("Failed to create companion")
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{companionId}")
    @Operation(summary = "동행 취소")
    fun deleteCompanion(
        @PathVariable companionId: Long,
        @RequestHeader("Authorization") authorizationHeader: String
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        companionService.deleteCompanion(companionId, email)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/companions/mine")
    @Operation(summary = "내 동행 찾기")
    fun getMyCompanions(
        @RequestHeader("Authorization") authorizationHeader: String
    ): ResponseEntity<Map<String, List<CompanionResponse>>> {
        val email = getEmailFromHeader(authorizationHeader)
        val user = getUserFromEmail(email)

        val userId = user.id

        val asLeader = companionService.getCompanionAsLeader(userId).orEmpty()
        val asMember = companionService.getCompanionAsMember(userId).orEmpty()

        return ResponseEntity.ok(
            mapOf(
                "asLeader" to asLeader,
                "asMember" to asMember
            )
        )
    }

    @PatchMapping("/{companionId}/title")
    @Operation(summary = "동행 이름 수정")
    fun updateCompanionTitle(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable companionId: Long,
        @RequestBody title: String
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        companionService.updateTitle(companionId, title, email)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{companionId}/status/completed")
    fun completeCompanion(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable companionId: Long
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        companionService.completeCompanion(companionId, email)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{companionId}/status/ongoing")
    fun restoreCompanion(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable companionId: Long
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        companionService.restoreCompanion(companionId, email)
        return ResponseEntity.noContent().build()
    }
}
