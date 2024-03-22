package com.excel.sheet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "userdata")
@Entity
public class UserData {
	    @Id
	    private int sno;
	    private String name;
	    private double mobile;
	   
		public int getSno() {
			return sno;
		}
		public void setSno(int sno) {
			this.sno = sno;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getMobile() {
			return mobile;
		}
		public void setMobile(double d) {
			this.mobile = d;
		}

	
}
