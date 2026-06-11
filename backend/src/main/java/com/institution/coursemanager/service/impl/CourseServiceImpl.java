package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.dto.CourseCreateDTO;
import com.institution.coursemanager.dto.CourseUpdateDTO;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.entity.CourseCategory;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.exception.NotFoundException;
import com.institution.coursemanager.exception.ConflictException;
import com.institution.coursemanager.exception.ValidationException;
import com.institution.coursemanager.mapper.CourseCategoryMapper;
import com.institution.coursemanager.mapper.CourseMapper;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.CourseService;
import com.institution.coursemanager.vo.AdminCourseVO;
import com.institution.coursemanager.vo.PageResult;
import com.institution.coursemanager.vo.PublicCourseDetailVO;
import com.institution.coursemanager.vo.PublicCourseVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int NOT_DELETED = 0;

    private final CourseCategoryMapper courseCategoryMapper;
    private final EnrollmentOrderMapper enrollmentOrderMapper;

    public CourseServiceImpl(CourseCategoryMapper courseCategoryMapper, EnrollmentOrderMapper enrollmentOrderMapper) {
        this.courseCategoryMapper = courseCategoryMapper;
        this.enrollmentOrderMapper = enrollmentOrderMapper;
    }

    @Override
    public AdminCourseVO createCourse(CourseCreateDTO dto) {
        validateCreateDTO(dto);

        // 校验分类是否存在
        CourseCategory category = courseCategoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new NotFoundException("课程分类不存在");
        }
        LocalDateTime now = LocalDateTime.now();

        Course course = new Course();
        course.setCategoryId(dto.getCategoryId());
        course.setTitle(dto.getTitle());
        course.setSubtitle(dto.getSubtitle());
        course.setCoverImage(dto.getCoverImage());
        course.setDescription(dto.getDescription());
        course.setTeacherName(dto.getTeacherName());
        course.setPrice(dto.getPrice());
        course.setRegistrationFee(dto.getRegistrationFee());
        course.setStatus(CourseStatus.DRAFT.getCode());
        course.setDeleted(NOT_DELETED);
        course.setCreateTime(now);
        course.setUpdateTime(now);
        save(course);
        return toAdminCourseVO(course);
    }

    @Override
    public AdminCourseVO updateCourse(Long id, CourseUpdateDTO dto) {
        Course course = requireCourse(id);
        if (dto == null) {
            throw new ValidationException("课程更新内容不能为空");
        }
        if (dto.getCategoryId() != null) {
            course.setCategoryId(dto.getCategoryId());
        }
        if (dto.getTitle() != null) {
            if (!StringUtils.hasText(dto.getTitle())) {
                throw new ValidationException("课程标题不能为空");
            }
            course.setTitle(dto.getTitle());
        }
        if (dto.getSubtitle() != null) {
            course.setSubtitle(dto.getSubtitle());
        }
        if (dto.getCoverImage() != null) {
            course.setCoverImage(dto.getCoverImage());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
        if (dto.getTeacherName() != null) {
            course.setTeacherName(dto.getTeacherName());
        }
        if (dto.getPrice() != null) {
            validateMoney(dto.getPrice(), "课程价格不能为负");
            course.setPrice(dto.getPrice());
        }
        if (dto.getRegistrationFee() != null) {
            validateMoney(dto.getRegistrationFee(), "报名费不能为负");
            course.setRegistrationFee(dto.getRegistrationFee());
        }
        course.setUpdateTime(LocalDateTime.now());
        updateById(course);
        return toAdminCourseVO(course);
    }

    @Override
    public void deleteCourse(Long id) {
        requireCourse(id);

        // 检查是否存在未完结的报名订单
        Long orderCount = enrollmentOrderMapper.selectCount(
                new LambdaQueryWrapper<EnrollmentOrder>()
                        .eq(EnrollmentOrder::getCourseId, id)
                        .in(EnrollmentOrder::getEnrollmentStatus,
                                EnrollmentStatus.PENDING.getCode(),
                                EnrollmentStatus.CONTACTED.getCode())
        );
        if (orderCount > 0) {
            throw new ConflictException("该课程存在未完结的报名订单，无法删除");
        }
        removeById(id);
    }

    @Override
    public void onlineCourse(Long id) {
        Course course = requireCourse(id);
        if (!StringUtils.hasText(course.getTitle())) {
            throw new ValidationException("课程标题不能为空，不能上架");
        }
        course.setStatus(CourseStatus.ONLINE.getCode());
        course.setUpdateTime(LocalDateTime.now());
        updateById(course);
    }

    @Override
    public void offlineCourse(Long id) {
        Course course = requireCourse(id);
        course.setStatus(CourseStatus.OFFLINE.getCode());
        course.setUpdateTime(LocalDateTime.now());
        updateById(course);
    }

    @Override
    public AdminCourseVO getAdminCourseDetail(Long id) {
        return toAdminCourseVO(requireCourse(id));
    }

    @Override
    public PublicCourseDetailVO getPublicCourseDetail(Long id) {
        Course course = requireCourse(id);
        if (!CourseStatus.ONLINE.getCode().equals(course.getStatus())) {
            throw new NotFoundException("课程不存在或未上架");
        }
        PublicCourseDetailVO vo = new PublicCourseDetailVO();
        vo.setId(course.getId());
        vo.setCategoryId(course.getCategoryId());
        vo.setCategoryName(getCategoryName(course.getCategoryId()));
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setCoverImage(course.getCoverImage());
        vo.setTeacherName(course.getTeacherName());
        vo.setPrice(course.getPrice());
        vo.setRegistrationFee(course.getRegistrationFee());
        vo.setDescription(course.getDescription());
        return vo;
    }

    @Override
    public PageResult<AdminCourseVO> getAdminCoursePage(Integer pageNum, Integer pageSize, String keyword, String status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .like(StringUtils.hasText(keyword), Course::getTitle, keyword)
                .eq(StringUtils.hasText(status), Course::getStatus, status)
                .orderByDesc(Course::getCreateTime);
        Page<Course> page = page(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)), wrapper);
        List<AdminCourseVO> records = page.getRecords().stream()
                .map(this::toAdminCourseVO)
                .toList();
        return PageResult.of(page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), records);
    }

    @Override
    public PageResult<PublicCourseVO> getPublicCoursePage(Integer pageNum, Integer pageSize, Long categoryId, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, CourseStatus.ONLINE.getCode())
                .eq(categoryId != null, Course::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), Course::getTitle, keyword)
                .orderByDesc(Course::getCreateTime);
        Page<Course> page = page(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)), wrapper);
        List<PublicCourseVO> records = page.getRecords().stream()
                .map(this::toPublicCourseVO)
                .toList();
        return PageResult.of(page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), records);
    }

    private void validateCreateDTO(CourseCreateDTO dto) {
        if (dto == null) {
            throw new ValidationException("课程创建内容不能为空");
        }
        if (dto.getCategoryId() == null) {
            throw new ValidationException("分类ID不能为空");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new ValidationException("课程标题不能为空");
        }
        validateMoney(dto.getPrice(), "课程价格不能为空且不能为负");
        validateMoney(dto.getRegistrationFee(), "报名费不能为空且不能为负");
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(message);
        }
    }

    private Course requireCourse(Long id) {
        if (id == null) {
            throw new ValidationException("课程ID不能为空");
        }
        Course course = getById(id);
        if (course == null) {
            throw new NotFoundException("课程不存在");
        }
        return course;
    }

    private AdminCourseVO toAdminCourseVO(Course course) {
        AdminCourseVO vo = new AdminCourseVO();
        vo.setId(course.getId());
        vo.setCategoryId(course.getCategoryId());
        vo.setCategoryName(getCategoryName(course.getCategoryId()));
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setCoverImage(course.getCoverImage());
        vo.setDescription(course.getDescription());
        vo.setTeacherName(course.getTeacherName());
        vo.setPrice(course.getPrice());
        vo.setRegistrationFee(course.getRegistrationFee());
        vo.setStatus(toCourseStatus(course.getStatus()));
        vo.setCreateTime(format(course.getCreateTime()));
        vo.setUpdateTime(format(course.getUpdateTime()));
        return vo;
    }

    private PublicCourseVO toPublicCourseVO(Course course) {
        PublicCourseVO vo = new PublicCourseVO();
        vo.setId(course.getId());
        vo.setCategoryId(course.getCategoryId());
        vo.setCategoryName(getCategoryName(course.getCategoryId()));
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setCoverImage(course.getCoverImage());
        vo.setTeacherName(course.getTeacherName());
        vo.setPrice(course.getPrice());
        vo.setRegistrationFee(course.getRegistrationFee());
        return vo;
    }

    private CourseStatus toCourseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return CourseStatus.DRAFT;
        }
        return CourseStatus.fromCode(status);
    }

    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        CourseCategory category = courseCategoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }

    private static final int MAX_PAGE_SIZE = 100;

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
