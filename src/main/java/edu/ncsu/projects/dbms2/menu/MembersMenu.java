package edu.ncsu.projects.dbms2.menu;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.ncsu.projects.dbms2.dao.DiscountDao;
import edu.ncsu.projects.dbms2.dao.MemberDao;
import edu.ncsu.projects.dbms2.dao.ProductDao;
import edu.ncsu.projects.dbms2.entity.Member;
import edu.ncsu.projects.dbms2.entity.MemberTransaction;
import edu.ncsu.projects.dbms2.entity.MemberTransactionsInvolve;

@Component
public class MembersMenu {
	private final List<String> menuList = new ArrayList<>();
	private static Scanner scan = new Scanner(System.in);
	
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private ProductDao productDao;
	
	@Autowired
	private DiscountDao discountDao;
	
	public MembersMenu() {
		menuList.add("View all Members");
		menuList.add("View Member details by attribute");
		menuList.add("Add new Member");
		menuList.add("Update Member by ID");
		menuList.add("Delete Member by ID");
		menuList.add("Remove Member record from DB");
		menuList.add("Add Member Transaction");
		menuList.add("Generate Member Transaction Bill");
		menuList.add("Add Member Returns");
		menuList.add("Back to Main Menu");
	}

	private void executeAction(int choice) {
		switch(choice) {
		case 1: 
			getAllMembers();
			break;
		case 2:
			getMemberByAttribute();
			break;
		case 3:
			addMember();
			break;
		case 4:
			updateMember();
			break;
		case 5:
			deleteMember();
			break;
		case 6:
			removeRecordFromDb();
			break;
		case 7:
			addMemberTransaction("ORDER");
			break;
		case 8:
			generateTransactionBill();
			break;
		case 9:
			addMemberTransaction("RETURN");
			break;
		default:
			System.out.println("Invalid Choice");
		}
	}
	
	private void generateTransactionBill() {
		System.out.println("Enter transaction ID: ");
		Integer transactionId = scan.nextInt();
		
		List<MemberTransactionsInvolve> memberTransactionDetails = memberDao.getMemberTransactionDetails(transactionId);
		Double transactionTotalPrice = memberDao.generateMemberTransactionTotalPrice(transactionId);
		
		for (MemberTransactionsInvolve details : memberTransactionDetails) {
			System.out.println(details);
		}
		
		System.out.println("Total Price of the Transaction: "+ transactionTotalPrice);
	}

	private void removeRecordFromDb() {
		System.out.println("Enter member ID to remove: ");
		Integer memberId = scan.nextInt(); 
		
		int count = memberDao.removeFromDb(memberId);
		System.out.println("Successfully removed "+ count +" records from MEMBERS table.");
	}

	private void addMemberTransaction(String transactionType) {
		System.out.println("Enter member ID: ");
		Integer memberId = scan.nextInt();
		
		System.out.println("Enter cashier ID: ");
		Integer cashierId = scan.nextInt();
		
		System.out.println("Enter store ID: ");
		Integer storeId = scan.nextInt();
		
		MemberTransaction transaction = new MemberTransaction();
		transaction.setCashierId(cashierId);
		transaction.setMemberId(memberId);
		transaction.setStoreId(storeId);
		transaction.setTransactionDate(new Date(System.currentTimeMillis()));
		transaction.setTransactionType(transactionType);
		
		Integer transactionId = memberDao.addMemberTransaction(transaction);
		
		System.out.println("Enter number of products returned: ");
		int returnCount = scan.nextInt();
		
		List<MemberTransactionsInvolve> transactionDetailsList = new ArrayList<>(); 
		for (int i=0; i<returnCount; i++) {
			System.out.println("Enter product ID for product "+ (i+1) +": ");
			Integer productId = scan.nextInt();
			
			System.out.println("Enter return quantity for product "+ (i+1) +": ");
			Integer quantity = scan.nextInt();
			
			MemberTransactionsInvolve details = new MemberTransactionsInvolve();
			details.setTransactionId(transactionId);
			details.setProductId(productId);
			details.setProductQuantity(quantity);
			details.setStoreId(storeId);
			
			details.setDiscountId(discountDao.getStoreProductDiscount(storeId, productId));
			
			Double storeProductPrice = productDao.getStoreProductPrice(productId, storeId);
			details.setPrice(storeProductPrice);
			
			Double storeProductDiscountPercent = discountDao.getStoreProductDiscountPercent(storeId, productId);
			
			Double discountedStoreProductPrice = storeProductPrice - (storeProductPrice * storeProductDiscountPercent / 100);
			details.setTotalPrice(discountedStoreProductPrice);
			
			transactionDetailsList.add(details);
		}
		
		memberDao.addMemberTransactionDetails(transactionDetailsList);
		
		System.out.println("Entered the transaction successfully!");
	}
	
	private void deleteMember() {
		System.out.println("Enter member ID to delete: ");
		Integer memberId = scan.nextInt();
		
		int deletedMember = memberDao.deleteMember(memberId);
		
		System.out.println("Deleted "+ deletedMember +" rows.");
	}

	private void updateMember() {
		System.out.println("Enter attribute name: ");
		String attributeName = scan.next().toUpperCase();
		
		scan.nextLine();
		System.out.println("Enter attribute value: ");
		String attributeValue = scan.nextLine();
		
		System.out.println("Enter member ID to update: ");
		Integer memberId = scan.nextInt();
		
		int updateCount = memberDao.updateByAttribute(attributeName, attributeValue, memberId);
		
		System.out.println("Updated "+ updateCount +" rows in MEMBERS table.");
	}

	private void addMember() {
		Member member = new Member();
		
		scan.nextLine();
		System.out.println("Enter first name: ");
		member.setFirstName(scan.nextLine());
		
		scan.nextLine();
		System.out.println("Enter last name: ");
		member.setLastName(scan.nextLine());
		
		System.out.println("Enter active status (true/false): ");
		member.setActiveStatus(scan.nextBoolean());
		
		scan.nextLine();
		System.out.println("Enter address: ");
		member.setAddress(scan.nextLine());
		
		System.out.println("Enter email: ");
		member.setEmail(scan.next());
		
		System.out.println("Enter membership level: ");
		member.setMembershipLevel(scan.next());
		
		System.out.println("Enter phone: ");
		member.setPhone(scan.next());
		
		int count = memberDao.addMember(member);
		System.out.println("Added "+ count +" rows.");
	}

	private void getMemberByAttribute() {
		System.out.print("Enter attribute name: ");
		String attributeName = scan.next().toUpperCase();
		
		scan.nextLine();
		System.out.println("Enter attribute value: ");
		String attributeValue = scan.nextLine();
		
		Member member = memberDao.findByAttribute(attributeName, attributeValue);
		
		System.out.println(member);
	}

	private void getAllMembers() {
		List<Member> members = memberDao.findAll();
		
		for(Member member : members) {
			System.out.println(member);
		}
	}
	
	public void loadMenu() {
		while (true) {
			printCustomerMenu();
			
			System.out.print("Enter choice: ");
			int choice = scan.nextInt();
			
			if (choice == menuList.size()) {
				break;
			}
			
			try {
				executeAction(choice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printCustomerMenu() {
		System.out.println("CUSTOMER ACTIONS:");
		for (int i=0; i<menuList.size(); i++) {
			System.out.println(i+1 +": "+ menuList.get(i));
		}
	}
}
