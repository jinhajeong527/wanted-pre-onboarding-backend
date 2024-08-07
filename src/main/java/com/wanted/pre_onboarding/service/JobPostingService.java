package com.wanted.pre_onboarding.service;

import com.wanted.pre_onboarding.domain.Company;
import com.wanted.pre_onboarding.domain.JobPosting;
import com.wanted.pre_onboarding.domain.vo.Address;
import com.wanted.pre_onboarding.dto.request.JobPostingRequest;
import com.wanted.pre_onboarding.dto.response.JobPostingDetail;
import com.wanted.pre_onboarding.dto.response.JobPostingResponse;
import com.wanted.pre_onboarding.dto.response.JobPostingSummary;
import com.wanted.pre_onboarding.exception.EntityNotFoundException;
import com.wanted.pre_onboarding.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final CompanyService companyService;

    @Transactional
    public JobPostingResponse register(JobPostingRequest request) {

        Company foundCompany = companyService.findCompanyById(request.getCompanyId());

        JobPosting jobPosting = JobPosting.builder()
                .position(request.getPosition())
                .description(request.getDescription())
                .reward(request.getReward())
                .usedSkills(request.getUsedSkills())
                .company(foundCompany)
                .build();

        JobPosting saved = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.fromEntity(saved);
    }
    @Transactional
    public JobPostingResponse edit(Long postId, JobPostingRequest request) {
        companyService.findCompanyById(request.getCompanyId());
        JobPosting foundJp = findJobPosting(postId);

        foundJp.updatePosition(request.getPosition());
        foundJp.updateReward(request.getReward());
        foundJp.updateUsedSkills(request.getUsedSkills());
        foundJp.updateDescription(request.getDescription());

        return JobPostingResponse.fromEntity(foundJp);
    }

    @Transactional
    public void delete(Long postId) {
        jobPostingRepository.delete(findJobPosting(postId));
    }

    @Transactional(readOnly = true)
    public List<JobPostingSummary> list() {
        return jobPostingRepository.findByIsOpenTrue()
                .stream()
                .map(JobPostingSummary::fromEntity)
                .toList();
    }
    @Transactional(readOnly = true)
    public JobPostingDetail getDetail(Long postId) {
        JobPosting jobPosting = findJobPosting(postId);

        Company hiring = jobPosting.getCompany();
        Address address = hiring.getAddress();

        List<Long> list = jobPostingRepository.findByCompanyAndExcludeId(hiring, postId)
                .stream()
                .map(JobPosting::getId)
                .toList();

        return JobPostingDetail.builder()
                .postId(jobPosting.getId())
                .companyName(hiring.getName())
                .country(address.getCountry())
                .region(address.getDistrict())
                .position(jobPosting.getPosition())
                .reward(jobPosting.getReward())
                .description(jobPosting.getDescription())
                .usedSkills(jobPosting.getUsedSkills())
                .others(list)
                .build();
    }

    public JobPosting findJobPosting(Long postId) {
        return jobPostingRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("요청한 채용공고를 찾지 못했습니다."));
    }
}
