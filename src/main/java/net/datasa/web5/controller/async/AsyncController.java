package net.datasa.web5.controller.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/async")
public class AsyncController {
	
	@GetMapping("/concept")
	public String page1() {return "asyncView/1. concept";}
	
	@GetMapping("/text")
	public String page2() {return "asyncView/2. text";}
	
	@GetMapping("/object")
	public String page3() {return "asyncView/3. object";}
	
}
