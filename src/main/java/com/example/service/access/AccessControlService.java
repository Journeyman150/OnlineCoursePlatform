package com.example.service.access;

import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
    private IdCheckService idCheckService;
    @Autowired
    public AccessControlService(IdCheckService idCheckService) {
        this.idCheckService = idCheckService;
    }
    public boolean authorHasAccessToCourse(User author, long courseId) {
        if (idCheckService.getAuthorIdByCourseId(courseId) == author.getId()) {
            return true;
        } else return false;
    }
}
