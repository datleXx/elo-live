package com.bookie.replay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplayService {
    @Transactional
    public void reset(String competition, List<List<ReplayRow>> allRows) {

    }
}
