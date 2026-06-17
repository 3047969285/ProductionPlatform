package com.devflow.common;

import com.devflow.model.dto.*;
import com.devflow.model.entity.*;
import com.devflow.model.vo.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model DTO VO 转换工具
 */
public final class BeanConvert {

    /**
     * 私有构造防止实例化
     */
    private BeanConvert() {}

    // ── User ──────────────────────────────────────────────────────────────

    /**
     * 用户实体转视图
     *
     * @param m 用户实体
     * @return 用户视图不含密码未找到返回空
     */
    public static UserVo toVo(User m) {
        if (m == null) return null;
        // 逐字段拷贝到视图对象
        UserVo v = new UserVo();
        v.setId(m.getId());
        v.setUsername(m.getUsername());
        v.setNickname(m.getNickname());
        v.setRole(m.getRole());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }

    /**
     * 用户 DTO 转实体
     *
     * @param d 用户 DTO
     * @return 用户实体
     */
    public static User toModel(UserDto d) {
        if (d == null) return null;
        User m = new User();
        m.setId(d.getId());
        m.setUsername(d.getUsername());
        m.setPassword(d.getPassword()); // 明文或已哈希，由 Service 层处理
        m.setNickname(d.getNickname());
        m.setRole(d.getRole());
        return m;
    }

    /**
     * 批量转换用户实体为视图列表
     *
     * @param list 用户实体列表
     * @return 用户视图列表
     */
    public static List<UserVo> toUserVoList(List<User> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── Project ───────────────────────────────────────────────────────────

    /**
     * 项目实体转视图
     *
     * @param m 项目实体
     * @return 项目视图未找到返回空
     */
    public static ProjectVo toVo(Project m) {
        if (m == null) return null;
        ProjectVo v = new ProjectVo();
        v.setId(m.getId());
        v.setCode(m.getCode());
        v.setName(m.getName());
        v.setDescription(m.getDescription());
        v.setTechStack(m.getTechStack());
        v.setDeliveryType(m.getDeliveryType());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }

    /**
     * 项目 DTO 转实体
     *
     * @param d 项目 DTO
     * @return 项目实体
     */
    public static Project toModel(ProjectDto d) {
        if (d == null) return null;
        Project m = new Project();
        m.setId(d.getId());
        m.setCode(d.getCode());
        m.setName(d.getName());
        m.setDescription(d.getDescription());
        m.setTechStack(d.getTechStack());
        m.setDeliveryType(d.getDeliveryType());
        return m;
    }

    /**
     * 批量转换项目实体为视图列表
     *
     * @param list 项目实体列表
     * @return 项目视图列表
     */
    public static List<ProjectVo> toProjectVoList(List<Project> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── DevTeam ───────────────────────────────────────────────────────────

    /**
     * 研发团队实体转视图
     *
     * @param m 团队实体
     * @return 团队视图未找到返回空
     */
    public static DevTeamVo toVo(DevTeam m) {
        if (m == null) return null;
        DevTeamVo v = new DevTeamVo();
        v.setId(m.getId());
        v.setCode(m.getCode());
        v.setName(m.getName());
        v.setCapacity(m.getCapacity());
        v.setStatus(m.getStatus());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }

    /**
     * 研发团队 DTO 转实体
     *
     * @param d 团队 DTO
     * @return 团队实体
     */
    public static DevTeam toModel(DevTeamDto d) {
        if (d == null) return null;
        DevTeam m = new DevTeam();
        m.setId(d.getId());
        m.setCode(d.getCode());
        m.setName(d.getName());
        m.setCapacity(d.getCapacity());
        m.setStatus(d.getStatus());
        return m;
    }

    /**
     * 批量转换团队实体为视图列表
     *
     * @param list 团队实体列表
     * @return 团队视图列表
     */
    public static List<DevTeamVo> toDevTeamVoList(List<DevTeam> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── DocFolder ─────────────────────────────────────────────────────────

    /**
     * 文档目录实体转视图
     *
     * @param m 目录实体
     * @return 目录视图未找到返回空
     */
    public static DocFolderVo toVo(DocFolder m) {
        if (m == null) return null;
        DocFolderVo v = new DocFolderVo();
        v.setId(m.getId());
        v.setProjectId(m.getProjectId());
        v.setModuleType(m.getModuleType());
        v.setParentId(m.getParentId());
        v.setName(m.getName());
        v.setSortOrder(m.getSortOrder());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }

    /**
     * 文档目录 DTO 转实体
     *
     * @param d 目录 DTO
     * @return 目录实体
     */
    public static DocFolder toModel(DocFolderDto d) {
        if (d == null) return null;
        DocFolder m = new DocFolder();
        m.setId(d.getId());
        m.setProjectId(d.getProjectId());
        m.setModuleType(d.getModuleType());
        m.setParentId(d.getParentId());
        m.setName(d.getName());
        m.setSortOrder(d.getSortOrder());
        return m;
    }

    /**
     * 批量转换目录实体为视图列表
     *
     * @param list 目录实体列表
     * @return 目录视图列表
     */
    public static List<DocFolderVo> toDocFolderVoList(List<DocFolder> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── Requirement ───────────────────────────────────────────────────────

    /**
     * 需求实体转视图
     *
     * @param m 需求实体
     * @return 需求视图未找到返回空
     */
    public static RequirementVo toVo(Requirement m) {
        return toVo(m, null, null);
    }

    /**
     * 需求实体转视图并填充关联名称
     *
     * @param m 需求实体
     * @param projectName 项目名称可为空
     * @param folderName 目录名称可为空
     * @return 需求视图未找到返回空
     */
    public static RequirementVo toVo(Requirement m, String projectName, String folderName) {
        if (m == null) return null;
        RequirementVo v = new RequirementVo();
        v.setId(m.getId());
        v.setReqNo(m.getReqNo());
        v.setTitle(m.getTitle());
        v.setContent(m.getContent());
        v.setProjectId(m.getProjectId());
        v.setFolderId(m.getFolderId());
        v.setProjectName(projectName); // 非表字段，来自 JOIN
        v.setFolderName(folderName);
        v.setPriority(m.getPriority());
        v.setStatus(m.getStatus());
        v.setProposer(m.getProposer());
        v.setOwner(m.getOwner());
        v.setCreatedAt(m.getCreatedAt());
        v.setUpdatedAt(m.getUpdatedAt());
        return v;
    }

    /**
     * 需求 DTO 转实体
     *
     * @param d 需求 DTO
     * @return 需求实体
     */
    public static Requirement toModel(RequirementDto d) {
        if (d == null) return null;
        Requirement m = new Requirement();
        m.setId(d.getId());
        m.setReqNo(d.getReqNo());
        m.setTitle(d.getTitle());
        m.setContent(d.getContent());
        m.setProjectId(d.getProjectId());
        m.setFolderId(d.getFolderId());
        m.setPriority(d.getPriority());
        m.setStatus(d.getStatus());
        m.setProposer(d.getProposer());
        m.setOwner(d.getOwner());
        return m;
    }

    /**
     * 批量转换需求实体为视图列表
     *
     * @param list 需求实体列表
     * @return 需求视图列表
     */
    public static List<RequirementVo> toRequirementVoList(List<Requirement> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── ApiDoc ────────────────────────────────────────────────────────────

    /**
     * 接口文档实体转视图
     *
     * @param m 接口文档实体
     * @return 接口文档视图未找到返回空
     */
    public static ApiDocVo toVo(ApiDoc m) {
        return toVo(m, null, null);
    }

    /**
     * 接口文档实体转视图并填充关联名称
     *
     * @param m 接口文档实体
     * @param projectName 项目名称可为空
     * @param folderName 目录名称可为空
     * @return 接口文档视图未找到返回空
     */
    public static ApiDocVo toVo(ApiDoc m, String projectName, String folderName) {
        if (m == null) return null;
        ApiDocVo v = new ApiDocVo();
        v.setId(m.getId());
        v.setApiNo(m.getApiNo());
        v.setTitle(m.getTitle());
        v.setContent(m.getContent());
        v.setMethod(m.getMethod());
        v.setPath(m.getPath());
        v.setProjectId(m.getProjectId());
        v.setFolderId(m.getFolderId());
        v.setProjectName(projectName);
        v.setFolderName(folderName);
        v.setStatus(m.getStatus());
        v.setProposer(m.getProposer());
        v.setOwner(m.getOwner());
        v.setCreatedAt(m.getCreatedAt());
        v.setUpdatedAt(m.getUpdatedAt());
        return v;
    }

    /**
     * 接口文档 DTO 转实体
     *
     * @param d 接口文档 DTO
     * @return 接口文档实体
     */
    public static ApiDoc toModel(ApiDocDto d) {
        if (d == null) return null;
        ApiDoc m = new ApiDoc();
        m.setId(d.getId());
        m.setApiNo(d.getApiNo());
        m.setTitle(d.getTitle());
        m.setContent(d.getContent());
        m.setMethod(d.getMethod());
        m.setPath(d.getPath());
        m.setProjectId(d.getProjectId());
        m.setFolderId(d.getFolderId());
        m.setStatus(d.getStatus());
        m.setProposer(d.getProposer());
        m.setOwner(d.getOwner());
        return m;
    }

    /**
     * 批量转换接口文档实体为视图列表
     *
     * @param list 接口文档实体列表
     * @return 接口文档视图列表
     */
    public static List<ApiDocVo> toApiDocVoList(List<ApiDoc> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── TestItem ──────────────────────────────────────────────────────────

    /**
     * 测试项实体转视图
     *
     * @param m 测试项实体
     * @return 测试项视图未找到返回空
     */
    public static TestItemVo toVo(TestItem m) {
        return toVo(m, null);
    }

    /**
     * 测试项实体转视图并填充项目名称
     *
     * @param m 测试项实体
     * @param projectName 项目名称可为空
     * @return 测试项视图未找到返回空
     */
    public static TestItemVo toVo(TestItem m, String projectName) {
        if (m == null) return null;
        TestItemVo v = new TestItemVo();
        v.setId(m.getId());
        v.setProjectId(m.getProjectId());
        v.setProjectName(projectName);
        v.setTitle(m.getTitle());
        v.setDescription(m.getDescription());
        v.setProgress(m.getProgress());
        v.setOwner(m.getOwner());
        v.setProposer(m.getProposer());
        v.setStatus(m.getStatus());
        v.setCreatedAt(m.getCreatedAt());
        v.setUpdatedAt(m.getUpdatedAt());
        return v;
    }

    /**
     * 测试项 DTO 转实体
     *
     * @param d 测试项 DTO
     * @return 测试项实体
     */
    public static TestItem toModel(TestItemDto d) {
        if (d == null) return null;
        TestItem m = new TestItem();
        m.setId(d.getId());
        m.setProjectId(d.getProjectId());
        m.setTitle(d.getTitle());
        m.setDescription(d.getDescription());
        m.setProgress(d.getProgress());
        m.setOwner(d.getOwner());
        m.setProposer(d.getProposer());
        m.setStatus(d.getStatus());
        return m;
    }

    /**
     * 批量转换测试项实体为视图列表
     *
     * @param list 测试项实体列表
     * @return 测试项视图列表
     */
    public static List<TestItemVo> toTestItemVoList(List<TestItem> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }

    // ── OpsIssue ──────────────────────────────────────────────────────────

    /**
     * 运维问题实体转视图
     *
     * @param m 运维问题实体
     * @return 运维问题视图未找到返回空
     */
    public static OpsIssueVo toVo(OpsIssue m) {
        return toVo(m, null);
    }

    /**
     * 运维问题实体转视图并填充项目名称
     *
     * @param m 运维问题实体
     * @param projectName 项目名称可为空
     * @return 运维问题视图未找到返回空
     */
    public static OpsIssueVo toVo(OpsIssue m, String projectName) {
        if (m == null) return null;
        OpsIssueVo v = new OpsIssueVo();
        v.setId(m.getId());
        v.setProjectId(m.getProjectId());
        v.setProjectName(projectName);
        v.setTitle(m.getTitle());
        v.setContent(m.getContent());
        v.setSeverity(m.getSeverity());
        v.setStatus(m.getStatus());
        v.setOwner(m.getOwner());
        v.setReporter(m.getReporter());
        v.setCreatedAt(m.getCreatedAt());
        v.setUpdatedAt(m.getUpdatedAt());
        return v;
    }

    /**
     * 运维问题 DTO 转实体
     *
     * @param d 运维问题 DTO
     * @return 运维问题实体
     */
    public static OpsIssue toModel(OpsIssueDto d) {
        if (d == null) return null;
        OpsIssue m = new OpsIssue();
        m.setId(d.getId());
        m.setProjectId(d.getProjectId());
        m.setTitle(d.getTitle());
        m.setContent(d.getContent());
        m.setSeverity(d.getSeverity());
        m.setStatus(d.getStatus());
        m.setOwner(d.getOwner());
        m.setReporter(d.getReporter());
        return m;
    }

    /**
     * 批量转换运维问题实体为视图列表
     *
     * @param list 运维问题实体列表
     * @return 运维问题视图列表
     */
    public static List<OpsIssueVo> toOpsIssueVoList(List<OpsIssue> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(BeanConvert::toVo).collect(Collectors.toList());
    }
}
