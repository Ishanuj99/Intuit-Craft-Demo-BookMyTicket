package com.intuit.BookMyTicket.dto;


import lombok.Data;

import java.util.List;

@Data
public class LockSeatsRequest {
    private List<Long> seatNumbers;
}

