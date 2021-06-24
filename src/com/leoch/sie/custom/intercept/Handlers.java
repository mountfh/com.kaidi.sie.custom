package com.leoch.sie.custom.intercept;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import com.leoch.sie.custom.sap.models.BOMStruct;
import com.leoch.sie.custom.sap.models.PartModel;
import com.leoch.sie.custom.utils.MyQueryManager;
import com.leoch.sie.custom.utils.MyStatusUtil;
import com.leoch.sie.custom.utils.ReleaseStatusMap;
import com.smile.interceptor.api.MyTCCRDecision;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentEnvelopeType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.PropertyLayout;


public class Handlers {

	static JDialog dialog;
	private static JComboBox<String> box;
	private static JPanel propanel;
	private static JScrollPane jspanel;
	private static JPanel buttonPanel;
	private static JButton okbutton;
	private static JButton canbutton;
	public static List<TCComponent> partList = null;
	public static List<TCComponent> designList = null;
	public static List<TCComponent> documentList = null;
	public static List<TCComponent> list;
	private static String msg;
	public static Map<TCComponent, JComboBox<String>> statusMap = new HashMap<>();

	
	static String[] queryPropNames = { "名称", "类型" };

	private static MyQueryManager qmdoc;

	private static TCSession session;
	
	static List<TCComponentUser> unsentUsers;
	
	
	@SuppressWarnings("deprecation")
	public static String classifyAndSentMail(TCComponentTask task, TCCRDecision decision) {
		try {

//			boolean isECN = false;
//			String name = "";
			String taskType = task.getTaskType();
			if (taskType.equals("EPMDoTask") || decision.getIntValue() == 89) {
				TCComponent[] targets = task.getRelatedComponents("root_target_attachments");
				if (targets == null || targets.length < 1) {
					return "流程目标中无对象";
				} 
				TCSession session = (TCSession) AIFUtility.getDefaultSession();
				session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
				qmdoc = new MyQueryManager(MyQueryManager.QUERY_TYPE_NOMAL, session);
//				final Map<String, List<TCComponent>> docs = new HashMap<>();
//				Map<String, List<TCComponent>> designs = new HashMap<>();
				TCComponentItemRevision rev = null;
				for (int i = 0; i < targets.length; i++) {
					String type = targets[i].getType();
					if (type.equals("L8_DocumentRevision")) {
						// 文档归档
						rev = (TCComponentItemRevision) targets[i];
						String scname = rev.getProperty("l8_file_type_section");
						pigeonhole(rev, scname);
//						String cs = rev.getProperty("l8_file_type_class");
//						List<TCComponent> revs = docs.get(cs);
//						if (revs == null) {
//							revs = new ArrayList<>();
//						}
//						revs.add(rev);
//						docs.put(cs, revs);
					} else if (type.equals("L8_DesignRevision")) {
						// 图纸归档
						rev = (TCComponentItemRevision) targets[i];
						String scname = rev.getProperty("l8_drawing_type");
						pigeonhole(rev, scname);
//						String cs = rev.getProperty("l8_type");
//						List<TCComponent> revs = designs.get(cs);
//						if (revs == null) {
//							revs = new ArrayList<>();
//						}
//						revs.add(rev);
//						designs.put(cs, revs);
					} else if ("L8_ECN".equals(type)) {
//						isECN = true;
//						designs = new HashMap<>();
//						name = targets[i].getProperty("object_name");
						TCComponent[] solus = targets[i].getRelatedComponents("L8_SoluObject");
						for (int j = 0; j < solus.length; j++) {
							type = solus[j].getType();
							if (type.equals("L8_DocumentRevision")) {
								// 文档归档
								rev = (TCComponentItemRevision) solus[j];
								String scname = rev.getProperty("l8_file_type_section");
								pigeonhole(rev, scname);
//								String cs = rev.getProperty("l8_file_type_class");
//								List<TCComponent> revs = docs.get(cs);
//								if (revs == null) {
//									revs = new ArrayList<>();
//								}
//								revs.add(rev);
//								docs.put(cs, revs);
							} else if (type.equals("L8_DesignRevision")) {
								// 图纸归档
								rev = (TCComponentItemRevision) solus[j];
								String scname = rev.getProperty("l8_drawing_type");
								pigeonhole(rev, scname);
//								String cs = rev.getProperty("l8_type");
//								List<TCComponent> revs = designs.get(cs);
//								if (revs == null) {
//									revs = new ArrayList<>();
//								}
//								revs.add(rev);
//								designs.put(cs, revs);
							} 
						}
						break;
					}
				}
				session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
				
//				if (docs.size() == 0 && designs.size() == 0) {
//					return null;
//				}
//				final Map<String, List<TCComponentUser>> doc_models = new HashMap<>();
//				if (docs.size() != 0) {
//					Set<String> keys = docs.keySet();
//					String info = "";
//					// 按大类加载需要发送邮件的用户
//					for (String key : keys) {
//						List<TCComponent> revs = docs.get(key);
//						TypeModel model = new TypeModel(key, revs);
//						info += model.load();
//						unsentUsers = model.getUnReceivers();
//						if (info.isEmpty()) {
//							List<TCComponentUser> users = model.getReceivers();
//							doc_models.put(key, users);
//						}
//					}
//					if (!info.isEmpty()) {
//						return info;
//					}
//				}
//				
//				Map<String, List<TCComponentUser>> design_models = new HashMap<>();
//				if (designs.size() != 0) {
//					Set<String> keys = designs.keySet();
//					String info = "";
//					// 按大类加载需要发送邮件的用户
//					for (String key : keys) {
//						List<TCComponent> revs = designs.get(key);
//						TypeModel model = new TypeModel(key, revs);
//						info += model.load();
//						unsentUsers = model.getUnReceivers();
//						if (info.isEmpty()) {
//							List<TCComponentUser> users = model.getReceivers();
//							design_models.put(key, users);
//						}
//					}
//					if (!info.isEmpty()) {
//						return info;
//					}
//				}
//				
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//				String date = sdf.format(new Date());
//				
//				String envelopeName = "受控文档发布通知-" + date;
//				if (isECN) {
//					envelopeName = name + "发布通知-" + date;
//				}
//				envelopeName = "受控图纸发布通知-" + date;
//				sentMail(doc_models, docs, envelopeName);
//				if (isECN) {
//					envelopeName = name + "发布通知-" + date;
//				}
//				sentMail(design_models, designs, envelopeName);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}
	
	/**
	 * @Title: pigeonhole
	 * @Description: TODO(图纸文档归档)
	 * @param @param rev
	 * @param @param scname    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	public static void pigeonhole(TCComponentItemRevision rev,String scname) {
		String[] queryValues = { scname, "系统文件夹" };
		TCComponent[] result = qmdoc.runQuery(queryPropNames, queryValues);
		try {
			for (int i = 0; i < result.length; i++) {
				result[i].add("contents", rev.getItem());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	
	/**
	 * @Title: sentMail
	 * @Description: TODO(发送邮件)
	 * @param @param models
	 * @param @param docs
	 * @param @param envelopeName
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	public static void sentMail(Map<String, List<TCComponentUser>> models, Map<String, List<TCComponent>> docs,String envelopeName) throws TCException {
		if (models.size() != 0) {
			Set<String> keys = models.keySet();
			TCComponentEnvelopeType type = (TCComponentEnvelopeType) session.getTypeComponent("Envelope");
			// 按大类发送邮件
			for (String key : keys) {
				List<TCComponentUser> users = models.get(key);
				if (users == null || users.size() == 0) {
					continue;
				}
				// 添加邮件到接收者邮箱
				List<TCComponent> attachments = docs.get(key);
				for (int i = 0; i < users.size(); i++) {
					TCComponent[] atts = new TCComponent[attachments.size()];
					attachments.toArray(atts);
					TCComponentUser user = users.get(i);
					List<TCComponent> attachments_new = new ArrayList<>();
					if (unsentUsers.contains(user)) {
						for (int j = 0; j < attachments.size(); j++) {
							if (!attachments.get(j).getProperty("item_revision_id").equals("A0")) {
								attachments_new.add(attachments.get(j));
							}
						}
						if (attachments_new.size() != 0) {
							atts = new TCComponent[attachments_new.size()];
							attachments_new.toArray(atts);
						} else {
							continue;
						}
					}
					TCComponentEnvelope envelope = type.create(envelopeName, "", "Envelope");
					TCComponentUser[] us = new TCComponentUser[] { user };
					envelope.addAttachments(atts);
					envelope.addReceivers(us);
					envelope.changeOwner(user, user.getLoginGroup());
					TCComponentFolder mailBox = user.getMailBox();
					mailBox.add("contents", envelope);
					mailBox.refresh();
				}
			}
		}
	}

	/**
	 * 批量发布状
	 * 
	 * @param task
	 * @param decision
	 * @return
	 */
	
	public static String changeStatus(TCComponentTask task, TCCRDecision decision) {

		try {
			String taskType = task.getTaskType();
			documentList = new ArrayList<TCComponent>();
			partList = new ArrayList<TCComponent>();
			designList = new ArrayList<TCComponent>();
			if (taskType.equals("reviewTask") || decision.getIntValue() == MyTCCRDecision.APPROVE_DECISION_VALUE) {
				TCComponent[] targets = task.getRelatedComponents("root_target_attachments");// 获取到对象
				if (targets == null || targets.length == 0) {
					return "流程任务目标不能为空！";
				}
				for (int i = 0; i < targets.length; i++) {
					String type = targets[i].getType();
					if (type.contains("ECN")) {
						TCComponentItem rev = (TCComponentItem) targets[i];
						TCComponent[] solus = rev.getRelatedComponents("L8_SoluObject");// 获取ECN下面所有的伪文件夹
						for (int j = 0; j < solus.length; j++) {
							TCComponent solu = solus[j];
							String strType = solu.getType();
							if (strType.contains("Document")) {
								documentList.add(solu);
							} else if (strType.contains("Design")) {
								designList.add(solu);
							} else if (strType.contains("Part")) {
								partList.add(solu);
							}
						}
						break;
					}
				}
				openDialog();
			}
		} catch (TCException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return msg;
	}

	private static void openDialog() throws TCException {

		list = new ArrayList<TCComponent>();
		list.addAll(partList);
		list.addAll(documentList);
		list.addAll(designList);
		dialog = new JDialog();
		dialog.setSize(768, 600);
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle("变更管理发布状态");
		initpropanel();
		initbuttonPanel();
		propanel.setBackground(Color.white);
		propanel.setBorder(new EtchedBorder());
		jspanel = new JScrollPane(propanel);
		buttonPanel.setBackground(Color.white);
		buttonPanel.setBorder(new EtchedBorder());
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(BorderLayout.CENTER, jspanel);
		dialog.getContentPane().add(BorderLayout.SOUTH, buttonPanel);
		dialog.setModal(true);
		dialog.setVisible(true);
		
	}

	private static void initbuttonPanel() {
		buttonPanel = new JPanel();
		okbutton = new JButton("确认");
		buttonPanel.add(okbutton);
		canbutton = new JButton("取消");
		buttonPanel.add(canbutton);
		okbutton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				msg = null;
				try {
					for (TCComponent entry : statusMap.keySet()) {
						String status = (String) statusMap.get(entry).getSelectedItem();
						if (status == null || status.isEmpty()) {
							msg = "状态不能为空！";
							break;
						}
						String stage = entry.getProperty("release_status_list");
						String statusName = ReleaseStatusMap.getReleaseStatusName(status);
						if (!stage.equals(status)) {
							MyStatusUtil.setStatus(entry, statusName);
							// 9-6新增逻辑，失效状态的版本，将对象设置为失效状态
							if (status.equals("失效")) {
								if (entry instanceof TCComponentItemRevision ) {
									TCComponentItemRevision revision = (TCComponentItemRevision) entry;
									TCComponentItem item = revision.getItem();
									MyStatusUtil.setStatus(item, statusName);
								}
							}
							
							String type = entry.getType();
							if (type.contains("PartRevision")) {
								entry.setProperty(PartModel.PartSentSAPFlag, "false");
								entry.setLogicalProperty(BOMStruct.BOMSentSAPFlag, false);
							}
						}
						TCComponent[] childs = entry.getRelatedComponents();
						for (int i = 0; i < childs.length; i++) {
							TCComponent child = childs[i];
							if (child instanceof TCComponentDataset) {
								MyStatusUtil.setStatus(child, statusName);
							}
						}					
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg = e.toString();
				}
				dialog.dispose();
			}
		});

		canbutton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				msg = "未确认发布状态！";
				dialog.dispose();
			}
		});
	}


	private static void initpropanel() throws TCException {
		propanel = new JPanel(new PropertyLayout());
		propanel.setBorder(new EtchedBorder());
		propanel.setOpaque(true);
		propanel.setBackground(Color.WHITE);
		propanel.setVisible(true);
		for (int i = 0; i < list.size(); i++) {
			String tempid = null;
			String[] doccom1 = { "发布", "失效", "暂停" };
			String[] descom2 = { "发布", "失效", "暂停" };
			String[] partcom3 = { "原型研制", "工程试制", "批量" };
			String[] partcom4 = { "原型研制", "工程试制", "限用", "批量", "失效" };
			TCComponent rev = list.get(i);
			String status = rev.getProperty("release_status_list");
			tempid = rev.getProperty("item_id");
			if (partList.contains(rev)) {
				if (tempid.startsWith("201") || tempid.startsWith("202")) {
					box = new JComboBox<String>(partcom4);
				} else {
					box = new JComboBox<String>(partcom3);
				}
			} else if (documentList.contains(rev)) {
				box = new JComboBox<String>(doccom1);
			} else if (designList.contains(rev)) {
				box = new JComboBox<String>(descom2);
			}
			statusMap.put(rev, box);
//			if (status.isEmpty()) {
//				box.setSelectedIndex(-1);
//			} else {
//			}
			box.setSelectedItem(status);
			JLabel lb = new JLabel("  " + rev + " : ");
			propanel.add((i + 1) + ".1.left", lb);
			propanel.add((i + 1) + ".2.right", box);
		}

	}

}
