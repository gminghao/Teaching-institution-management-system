package com.institution.coursemanager.service;

import com.institution.coursemanager.dto.EnrollmentStatusDTO;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.exception.ConflictException;
import com.institution.coursemanager.exception.NotFoundException;
import com.institution.coursemanager.exception.ValidationException;
import com.institution.coursemanager.mapper.CourseMapper;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EnrollmentServiceImpl 状态转换规则测试
 *
 * 状态流转规则：
 * PENDING → CONTACTED / CANCELLED
 * CONTACTED → ENROLLED / CANCELLED
 * ENROLLED → 终态，不可变更
 * CANCELLED → 终态，不可变更
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private EnrollmentOrder createOrder(Long id, EnrollmentStatus status) {
        EnrollmentOrder order = new EnrollmentOrder();
        order.setId(id);
        order.setOrderNo("EN20260611001");
        order.setCourseId(1L);
        order.setCourseTitle("测试课程");
        order.setStudentName("测试学生");
        order.setStudentPhone("13800138000");
        order.setRegistrationFee(new BigDecimal("500.00"));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.UNPAID.getCode());
        order.setEnrollmentStatus(status.getCode());
        return order;
    }

    private EnrollmentStatusDTO createStatusDTO(String status) {
        EnrollmentStatusDTO dto = new EnrollmentStatusDTO();
        dto.setEnrollmentStatus(status);
        return dto;
    }

    @Nested
    @DisplayName("报名状态转换测试")
    class StatusTransitionTests {

        @Test
        @DisplayName("TC-STATUS-01: PENDING → CONTACTED 应成功")
        void pendingToContacted_shouldSucceed() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.PENDING);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);
            when(enrollmentOrderMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CONTACTED")));
            verify(enrollmentOrderMapper).updateById(argThat(o ->
                    EnrollmentStatus.CONTACTED.getCode().equals(o.getEnrollmentStatus())));
        }

        @Test
        @DisplayName("TC-STATUS-02: PENDING → CANCELLED 应成功")
        void pendingToCancelled_shouldSucceed() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.PENDING);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);
            when(enrollmentOrderMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CANCELLED")));
            verify(enrollmentOrderMapper).updateById(argThat(o ->
                    EnrollmentStatus.CANCELLED.getCode().equals(o.getEnrollmentStatus())));
        }

        @Test
        @DisplayName("TC-STATUS-03: PENDING → ENROLLED 应失败（跳级变更）")
        void pendingToEnrolled_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.PENDING);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("ENROLLED")));
            assertEquals("待处理状态只能变更为已联系或已取消", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-04: CONTACTED → ENROLLED 应成功")
        void contactedToEnrolled_shouldSucceed() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CONTACTED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);
            when(enrollmentOrderMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("ENROLLED")));
            verify(enrollmentOrderMapper).updateById(argThat(o ->
                    EnrollmentStatus.ENROLLED.getCode().equals(o.getEnrollmentStatus())));
        }

        @Test
        @DisplayName("TC-STATUS-05: CONTACTED → CANCELLED 应成功")
        void contactedToCancelled_shouldSucceed() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CONTACTED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);
            when(enrollmentOrderMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CANCELLED")));
            verify(enrollmentOrderMapper).updateById(argThat(o ->
                    EnrollmentStatus.CANCELLED.getCode().equals(o.getEnrollmentStatus())));
        }

        @Test
        @DisplayName("TC-STATUS-06: CONTACTED → PENDING 应失败（状态回退）")
        void contactedToPending_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CONTACTED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("PENDING")));
            assertEquals("已联系状态只能变更为已录取或已取消", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-07: ENROLLED → CONTACTED 应失败（终态不可变更）")
        void enrolledToContacted_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.ENROLLED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CONTACTED")));
            assertEquals("已录取的报名不能继续变更状态", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-08: ENROLLED → CANCELLED 应失败（终态不可变更）")
        void enrolledToCancelled_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.ENROLLED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CANCELLED")));
            assertEquals("已录取的报名不能继续变更状态", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-09: CANCELLED → PENDING 应失败（终态不可变更）")
        void cancelledToPending_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CANCELLED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("PENDING")));
            assertEquals("已取消的报名不能继续变更状态", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-10: CANCELLED → CONTACTED 应失败（终态不可变更）")
        void cancelledToContacted_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CANCELLED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("CONTACTED")));
            assertEquals("已取消的报名不能继续变更状态", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-11: CANCELLED → ENROLLED 应失败（终态不可变更）")
        void cancelledToEnrolled_shouldFail() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.CANCELLED);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("ENROLLED")));
            assertEquals("已取消的报名不能继续变更状态", exception.getMessage());
        }

        @Test
        @DisplayName("TC-STATUS-12: 相同状态转换应成功（幂等）")
        void sameStatusTransition_shouldSucceed() {
            EnrollmentOrder order = createOrder(1L, EnrollmentStatus.PENDING);
            when(enrollmentOrderMapper.selectById(1L)).thenReturn(order);
            when(enrollmentOrderMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("PENDING")));
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("TC-EDGE-01: 订单ID为空应抛出验证异常")
        void nullOrderId_shouldThrowValidationException() {
            assertThrows(ValidationException.class,
                    () -> enrollmentService.updateEnrollmentStatus(null, createStatusDTO("CONTACTED")));
        }

        @Test
        @DisplayName("TC-EDGE-02: 订单不存在应抛出未找到异常")
        void nonExistentOrder_shouldThrowNotFoundException() {
            when(enrollmentOrderMapper.selectById(999L)).thenReturn(null);

            assertThrows(NotFoundException.class,
                    () -> enrollmentService.updateEnrollmentStatus(999L, createStatusDTO("CONTACTED")));
        }

        @Test
        @DisplayName("TC-EDGE-03: 状态DTO为空应抛出验证异常")
        void nullStatusDTO_shouldThrowValidationException() {
            assertThrows(ValidationException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, null));
        }

        @Test
        @DisplayName("TC-EDGE-04: 状态值为空应抛出验证异常")
        void emptyStatus_shouldThrowValidationException() {
            assertThrows(ValidationException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("")));
        }

        @Test
        @DisplayName("TC-EDGE-05: 无效状态值应抛出验证异常")
        void invalidStatus_shouldThrowValidationException() {
            assertThrows(ValidationException.class,
                    () -> enrollmentService.updateEnrollmentStatus(1L, createStatusDTO("INVALID")));
        }
    }
}
