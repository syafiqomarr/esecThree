package com.ssm.pcert;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ssm.base.common.util.DateUtil;
import com.ssm.llp.base.common.Parameter;
import com.ssm.llp.base.common.model.LlpFileData;
import com.ssm.llp.base.common.sec.UserEnvironmentHelper;
import com.ssm.llp.base.common.service.LlpFileDataService;
import com.ssm.llp.base.common.service.WSManagementService;
import com.ssm.llp.base.page.WicketApplication;
import com.ssm.llp.base.sec.LlpUserEnviroment;
import com.ssm.llp.base.utils.WicketUtils;
import com.ssm.llp.base.wicket.SSMDownloadLink;
import com.ssm.llp.base.wicket.component.SSMAjaxButton;
import com.ssm.llp.base.wicket.component.SSMAjaxCheckBox;
import com.ssm.llp.base.wicket.component.SSMDateTextField;
import com.ssm.llp.base.wicket.component.SSMDropDownChoice;
import com.ssm.llp.base.wicket.component.SSMLabel;
import com.ssm.llp.base.wicket.component.SSMTextArea;
import com.ssm.llp.base.wicket.component.SSMTextField;
import com.ssm.llp.mod1.model.LlpUserProfile;
import com.ssm.llp.wicket.SSMAjaxFormSubmitBehavior;
import com.ssm.pcert.model.SecretaryPcertChangesForm;
import com.ssm.pcert.model.SecretaryPcertQualificationForm;
import com.ssm.pcert.model.SecretaryPcertReg;
import com.ssm.pcert.service.SecretaryPcertChangesFormService;
import com.ssm.pcert.service.SecretaryPcertRegService;
import com.ssm.webis.client.SSMInfoClient;
import com.ssm.webis.param.SSMCosecProfileReq;
import com.ssm.webis.param.SSMCosecProfileResp;
import org.apache.wicket.markup.html.form.Button;

@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class EditQualificationPanel extends PCertChangesBasePanel{
	@SpringBean(name = "SecretaryPcertChangesFormService")
	private SecretaryPcertChangesFormService secretaryPcertChangesFormService;
	
	@SpringBean(name = "SecretaryPcertRegService")
	private SecretaryPcertRegService secretaryPcertRegService;
	
	@Autowired
	@Qualifier("LlpFileDataService")
	LlpFileDataService llpFileDataService;
	
	@SpringBean(name="WSManagementService")
	WSManagementService wSManagementService;
	
	public EditQualificationPanel(String panelId, final SecretaryPcertChangesForm pcertChangesForm) {
		super(panelId);
		setDefaultModel(new CompoundPropertyModel(new LoadableDetachableModel() {
            protected Object load() {
            	SecretaryPcertQualificationForm pcertQualificationForm = new SecretaryPcertQualificationForm();
            	//Get from session
        		if(getSession().getAttribute("pcertChangesForm_") !=  null) {
        			SecretaryPcertChangesForm pcertChangesForm = (SecretaryPcertChangesForm) getSession().getAttribute("pcertChangesForm_");
        			
        			if(pcertChangesForm != null && pcertChangesForm.getPcertQualificationForm() != null){
        				pcertQualificationForm = pcertChangesForm.getPcertQualificationForm();
        				pcertQualificationForm.setPcertChangesFormRefNo(pcertChangesForm.getPcertChangesFormRefNo());
                	}else {
                		pcertQualificationForm.setPcertChangesFormRefNo(pcertChangesForm.getPcertChangesFormRefNo());
                		pcertChangesForm.setPcertQualificationForm(pcertQualificationForm);
                	}
        		}
            	return pcertQualificationForm;
            }
        }));
		add(new PcertQualificationForm("pcertQualificationForm",getDefaultModel(), pcertChangesForm));
	}
	
	private class PcertQualificationForm extends Form implements Serializable {
		
		final SecretaryPcertQualificationForm pcertQualificationForm;
		final RepeatingView listError;
		final SSMAjaxCheckBox declarationChkBox;
		
		public PcertQualificationForm(String id, IModel m,final SecretaryPcertChangesForm pcertChangesForm) {
			super(id, m);
			setPrefixLabelKey("page.lbl.pcert.qualification.");
			setAutoCompleteForm(false);
			pcertQualificationForm = (SecretaryPcertQualificationForm) m.getObject();
			
			//Current
			SecretaryPcertChangesForm pcertChangesFormCurr = (SecretaryPcertChangesForm) getSession().getAttribute("pcertChangesFormCurr_");
			
			final SSMLabel currProfType = new SSMLabel("currProfType", pcertChangesFormCurr.getPcertQualificationForm().getProfType());
			add(currProfType);
			
			final SSMLabel currProfNo = new SSMLabel("currProfNo", pcertChangesFormCurr.getPcertQualificationForm().getProfNo());
			add(currProfNo);
			
			final SSMLabel currProfExpiryDt = new SSMLabel("currProfExpiryDt", pcertChangesFormCurr.getPcertQualificationForm().getProfExpiryDt());
			add(currProfExpiryDt);
			
			final WebMarkupContainer wmcQualification = new WebMarkupContainer("wmcQualification");
			wmcQualification.setPrefixLabelKey("page.lbl.pcert.qualification.");
			wmcQualification.setOutputMarkupId(true);
			wmcQualification.setOutputMarkupPlaceholderTag(true);
			add(wmcQualification);
			
			final WebMarkupContainer wmcAttachment = new WebMarkupContainer("wmcAttachment");
			wmcAttachment.setPrefixLabelKey("page.lbl.pcert.qualification.");
			wmcAttachment.setOutputMarkupId(true);
			wmcAttachment.setOutputMarkupPlaceholderTag(true);
			wmcQualification.add(wmcAttachment);
			
			SSMAjaxCheckBox isQualification = new SSMAjaxCheckBox("isQualification", new PropertyModel(pcertChangesForm, "isQualificationChange") ) {
				@Override
				protected void onUpdate(AjaxRequestTarget aRT) {
					if (String.valueOf(true).equals(getValue())) {
						pcertChangesForm.setIsQualificationChange(true);
						wmcQualification.setVisible(true);
					}else{
						pcertChangesForm.setIsQualificationChange(false);
						wmcQualification.setVisible(false);
					}
					aRT.add(wmcQualification);
				}
				@Override
			    protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
			    {
			        super.updateAjaxAttributes( attributes );
			        String confirmTitle = resolve("page.lbl.pcert.qualification.confirmResetTitle");
			        String confirmDesc = resolve("page.lbl.pcert.qualification.confirmResetDesc");		
			        AjaxCallListener ajaxCallListener = generateAjaxConfirm(this, confirmTitle, confirmDesc, true);
			        attributes.getAjaxCallListeners().add( ajaxCallListener );
			    }
			};
			add(isQualification);
			
			if(!pcertChangesForm.getIsQualificationChange()){
				wmcQualification.setVisible(false);
			}
			
			final SSMDateTextField changeDt = new SSMDateTextField("changeDt");
			wmcQualification.add(changeDt);
			
			final SSMTextField profNo= new SSMTextField("profNo");
			wmcQualification.add(profNo);
			
			
			String msg = "";
			
			final SSMLabel warningDt= new SSMLabel("warningDt",msg);
			warningDt.setOutputMarkupId(true);
			warningDt.setOutputMarkupPlaceholderTag(true);
			warningDt.setEscapeModelStrings(false);
			wmcQualification.add(warningDt);
			SSMAjaxFormSubmitBehavior profExpiryDtOnBlur = new SSMAjaxFormSubmitBehavior("onchange", true) {
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					//Check on 30 days validity	
					Date expiryDate = pcertQualificationForm.getProfExpiryDt();
					Date dateToCheck = DateUtil.getAfterDate(new Date(), 30+1);
					
					if(expiryDate!=null){
						warningDt.setDefaultModelObject("");
						
						if(expiryDate.before(dateToCheck)) {

							String dtError =resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.profValidityMust30days");
							listError.add(new SSMLabel(listError.newChildId() , dtError));
							warningDt.setDefaultModelObject(dtError);
							
							
						
						}
					}
					
					
					target.add(warningDt);
					
					
				}
			};
			
			final SSMDateTextField profExpiryDt = new SSMDateTextField("profExpiryDt");
			profExpiryDt.add(profExpiryDtOnBlur);
			wmcQualification.add(profExpiryDt);
			
			final String jsValidationSearchLS = "jsValidationSearchLS";
			String mainFieldJsToValidate[] = new String[]{ "profType","profNo"};
			String mainFieldJsToValidateRules[] = new String[]{ "empty","empty"};
			setSemanticJSValidation(this, jsValidationSearchLS, mainFieldJsToValidate, mainFieldJsToValidateRules);
			
			final SSMAjaxButton searchLS = new SSMAjaxButton("searchLS",jsValidationSearchLS) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					
					SecretaryPcertQualificationForm qualificationForm = (SecretaryPcertQualificationForm) form.getDefaultModelObject();
					String jsScript = null;
					if(Parameter.PCERT_PROF_BODY_CODE_LS.equals(qualificationForm.getProfType()) ){
						
						try {
							String url = wSManagementService.getWsUrl("SSMInfoClient.getCoSecProfile");
							
							SSMCosecProfileReq req = new SSMCosecProfileReq();
							req.setAgencyId(Parameter.ROB_AGENCY_ID);
							
							final LlpUserProfile llpUserProfile = ((LlpUserEnviroment)UserEnvironmentHelper.getUserenvironment()).getLlpUserProfile();
							
							String cbsIdType = getCodeTypeWithValue(Parameter.ID_TYPE_CBS_MAPPING, llpUserProfile.getIdType());
							req.setIdType(cbsIdType);
							req.setId(llpUserProfile.getIdNo());
							
							SSMCosecProfileResp resp = new SSMCosecProfileResp();
							resp = SSMInfoClient.getCoSecProfile(url, req);
							
							if ("00".equals(resp.getSuccessCode())) {
								String lsNo = resp.getLsNo();
								Date lsExpDt = resp.getLsEndDt();								
								Date dateToCheck = DateUtil.getAfterDate(new Date(), 30+1);
								System.out.println("LSNO:"+lsNo);
								System.out.println("lsExpDt:"+lsExpDt);
								if(lsNo==null ||  !lsNo.equals(qualificationForm.getProfNo())) {
									String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.invalidLsNo");
									String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.invalidLsNo");
									jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
								}else if(lsExpDt.before(new Date())) {
									String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.expLsNo");
									String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.expLsNo");
									jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
								}else if(lsExpDt.before(dateToCheck)) {
									String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.profValidityMust30days");
									String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.profValidityMust30days");
									jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
								}
								
								qualificationForm.setProfExpiryDt(lsExpDt);
								
								
							}else {
								String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.webisError");
								String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.webisError");
								jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
							}
						
						} catch (Exception e) {
							String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.webisError");
							String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.webisError");
							jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
						}
						
						if(StringUtils.isNotBlank(jsScript)) {
							target.prependJavaScript(jsScript);
						}
						target.add(profExpiryDt);
						
					}
//					else {
//						//Check on 30 days validity						
//						Date expiryDate = qualificationForm.getProfExpiryDt();
//						Date dateToCheck = DateUtil.getAfterDate(new Date(), 30+1);
//						
//						if(expiryDate.before(dateToCheck)) {
//							String alertTitle = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertTitle.profValidityMust30days");
//							String alertDesc = WicketApplication.resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.profValidityMust30days");
//							jsScript = WicketUtils.generateAjaxErrorAlertScript(alertTitle, alertDesc);
//						}
//						
//						if(StringUtils.isNotBlank(jsScript)) {
//							target.prependJavaScript(jsScript);
//						}
//					}
					
				}
			};
			searchLS.setOutputMarkupId(true);
			searchLS.setOutputMarkupPlaceholderTag(true);
			wmcQualification.add(searchLS);
			
			SSMAjaxFormSubmitBehavior isProfTypeOnchange = new SSMAjaxFormSubmitBehavior("onchange", true) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					SecretaryPcertQualificationForm pcertQualificationForm = (SecretaryPcertQualificationForm) getForm().getDefaultModelObject();

					if(Parameter.PCERT_PROF_BODY_CODE_LS.equals(pcertQualificationForm.getProfType())) {
						searchLS.setVisible(true);
						profExpiryDt.setEnabled(false);
						wmcAttachment.setVisible(false);
						
					}else {
						searchLS.setVisible(false);
						profExpiryDt.setEnabled(true);
						wmcAttachment.setVisible(true);
					}	
					target.add(searchLS);
					target.add(profExpiryDt);
				}
			};			
			
			final SSMDropDownChoice profType = new SSMDropDownChoice("profType",Parameter.PROF_BODY_CODE, Parameter.PROF_BODY_TYPE_cs);
			profType.add(isProfTypeOnchange);
			wmcQualification.add(profType);
			
			if(Parameter.PCERT_PROF_BODY_CODE_LS.equals(profType)) {
				searchLS.setVisible(true);
				profExpiryDt.setEnabled(false);
			}else {
				searchLS.setVisible(false);
				profExpiryDt.setEnabled(true);
			}
			
			
			PcertFileUploadModel fileUploadModel = new PcertFileUploadModel();
			
			final FileUploadField fileUploadMembershipCert = new FileUploadField("fileUploadMembershipCert", new PropertyModel(fileUploadModel, "fileUploadMembershipCert"));
			wmcAttachment.add(fileUploadMembershipCert);
			
			final FileUploadField fileUploadRenewalReciept = new FileUploadField("fileUploadRenewalReciept", new PropertyModel(fileUploadModel, "fileUploadRenewalReciept"));
			wmcAttachment.add(fileUploadRenewalReciept);
			
			boolean hasUploadMembershipCert= false;
			boolean hasUploadRenewalReciept= false;
			
			SSMDownloadLink downloadnSuppDocMembershipCert = new SSMDownloadLink("downloadnSuppDocMembershipCert");
			downloadnSuppDocMembershipCert.setVisible(false);
			try {
				downloadnSuppDocMembershipCert.setDownloadData("membershipFile_"+pcertChangesForm.getPcertChangesFormRefNo()+".pdf", "application/pdf", pcertChangesForm.getPcertQualificationForm().getSuppDocMembershipCert().getFileData());
				downloadnSuppDocMembershipCert.setVisible(true);
				hasUploadMembershipCert= true;
			} catch (Exception e) {
				System.out.println("no downloadnSuppDocMembershipCert ");
			}
			wmcAttachment.add(downloadnSuppDocMembershipCert);
			
			SSMDownloadLink downloadSuppDocRenewalReciept = new SSMDownloadLink("downloadSuppDocRenewalReciept");
			downloadSuppDocRenewalReciept.setVisible(false);
			try {
				downloadSuppDocRenewalReciept.setDownloadData("renReciept_"+pcertChangesForm.getPcertChangesFormRefNo()+".pdf", "application/pdf", pcertChangesForm.getPcertQualificationForm().getSuppDocRenewalReciept().getFileData());
				downloadSuppDocRenewalReciept.setVisible(true);
				hasUploadRenewalReciept= true;
			} catch (Exception e) {
				System.out.println("no downloadnSuppDocMembershipCert ");
			}
			wmcAttachment.add(downloadSuppDocRenewalReciept);
			
			setMultiPart(true);
			
			final WebMarkupContainer wmcError = new WebMarkupContainer("wmcError");
			wmcError.setPrefixLabelKey("page.lbl.pcert.editSecretaryPcert.");
			wmcError.setOutputMarkupId(true);
			wmcError.setOutputMarkupPlaceholderTag(true);
			wmcError.setVisible(false);
			add(wmcError);
			
			final String jsValidationSubmitChanges = "jsValidationSubmitChanges";
			String mainFieldToValidate[] = new String[]{"changeDt","profNo","profExpiryDt","profType"};
			String mainFieldToValidateRules[] = new String[]{"notFutureDate","empty","empty","empty"};
			
			List<String> listMainFieldToValidate = new ArrayList<String>(Arrays.asList(mainFieldToValidate));
			List<String> listMainFieldToValidateRules = new ArrayList<String>(Arrays.asList(mainFieldToValidateRules));
			
			if(!hasUploadMembershipCert) {
				listMainFieldToValidate.add("fileUploadMembershipCert");
				listMainFieldToValidateRules.add("empty");
			}
			if(!hasUploadRenewalReciept) {
				listMainFieldToValidate.add("fileUploadRenewalReciept");
				listMainFieldToValidateRules.add("empty");
			}
			
			setSemanticJSValidation(this, jsValidationSubmitChanges, mainFieldToValidate, mainFieldToValidateRules);
			
			
			
			
			final SSMAjaxButton submitChanges = new SSMAjaxButton("submitChanges", jsValidationSubmitChanges) {
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					wmcError.setVisible(true);
					listError.removeAll();
					
					SecretaryPcertQualificationForm qualificationForm = (SecretaryPcertQualificationForm) form.getDefaultModelObject();
					
					if(!Parameter.PCERT_PROF_BODY_CODE_LS.equals(qualificationForm.getProfType())) {
						if(fileUploadMembershipCert.getFileUpload()!=null) {
							validateFileUpload(fileUploadMembershipCert);
							LlpFileData fileDataMembershipCert = new LlpFileData(fileUploadMembershipCert.getFileUpload().getBytes(),"PDF");
							qualificationForm.setSuppDocMembershipCert(fileDataMembershipCert);
						}
						
						if(fileUploadRenewalReciept.getFileUpload()!=null) {
							validateFileUpload(fileUploadRenewalReciept);
							LlpFileData fileDataRenewalReciept = new LlpFileData(fileUploadRenewalReciept.getFileUpload().getBytes(), "PDF");
							qualificationForm.setSuppDocRenewalReciept(fileDataRenewalReciept);
						}
					}
					
					//Check on 30 days validity	
					Date expiryDate = qualificationForm.getProfExpiryDt();
					Date dateToCheck = DateUtil.getAfterDate(new Date(), 30+1);
					
					if(expiryDate.before(dateToCheck)) {
						String id = profExpiryDt.getForm().getPrefixLabelKey()+profExpiryDt.getId();
						String idLableName = resolve(id);
						listError.add(new SSMLabel(listError.newChildId() , resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.profValidityMust30days", idLableName) ));
						
					}
					
					else {
						pcertChangesForm.setStatus(Parameter.PCERT_FORM_STATUS_IN_PROCESS);
						pcertChangesForm.setPcertQualificationForm(qualificationForm);
						SecretaryPcertReg pcertReg = secretaryPcertRegService.findByIdWithData(pcertChangesForm.getPcertRegNo());
						secretaryPcertChangesFormService.insertUpdateAll(pcertChangesForm, pcertReg);
						getSession().setAttribute("pcertChangesForm_",null);
						
						String msj = resolve("page.lbl.pcert.changes.successSubmit", pcertChangesForm.getPcertChangesFormRefNo());
						storeSuccessMsg(msj);
						setResponsePage(new DashboardPcert());
					}
					
					
				}
			};
			submitChanges.setEnabled(false);
			submitChanges.setOutputMarkupId(true);
			add(submitChanges);
			
			
			final SSMTextArea queryAnswer = new SSMTextArea("queryAnswer", Model.of(""));
			queryAnswer.setVisible(false);
			add(queryAnswer);
			
			final MultiLineLabel queryText = new MultiLineLabel("queryText",pcertChangesForm.getApproveRejectNotes());
			queryText.setOutputMarkupId(true);
			queryText.setOutputMarkupPlaceholderTag(true);
			queryText.setVisible(false);
			add(queryText);
			
			listError = new RepeatingView("listError");
			wmcError.add(listError);
			
			final String jsValidationResubmitChanges = "jsValidationResubmitChanges";
			String mainFieldToValidateResubmit[] = new String[]{ "changeDt","profNo","profExpiryDt","profType","queryAnswer"};
			String mainFieldToValidateRulesResubmit[] = new String[]{ "empty","empty","empty","empty","empty"};
			setSemanticJSValidation(this, jsValidationResubmitChanges, mainFieldToValidateResubmit, mainFieldToValidateRulesResubmit);
			
			final SSMAjaxButton reSubmit = new SSMAjaxButton("reSubmit",jsValidationResubmitChanges) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
	
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					wmcError.setVisible(false);
					listError.removeAll();
					
					
					
					SecretaryPcertQualificationForm qualificationForm = (SecretaryPcertQualificationForm) form.getDefaultModelObject();
					
					if(!Parameter.PCERT_PROF_BODY_CODE_LS.equals(qualificationForm.getProfType())) {
						if(fileUploadMembershipCert.getFileUpload()!=null) {
							validateFileUpload(fileUploadMembershipCert);
							LlpFileData fileDataMembershipCert = new LlpFileData(fileUploadMembershipCert.getFileUpload().getBytes(),"PDF");
							qualificationForm.setSuppDocMembershipCert(fileDataMembershipCert);
						}
						
						if(fileUploadRenewalReciept.getFileUpload()!=null) {
							validateFileUpload(fileUploadRenewalReciept);
							LlpFileData fileDataRenewalReciept = new LlpFileData(fileUploadRenewalReciept.getFileUpload().getBytes(), "PDF");
							qualificationForm.setSuppDocRenewalReciept(fileDataRenewalReciept);
						}
					}
					
					//Check on 30 days validity	
					Date expiryDate = qualificationForm.getProfExpiryDt();
					Date dateToCheck = DateUtil.getAfterDate(new Date(), 30+1);
					
					if(expiryDate.before(dateToCheck)) {
						String id = profExpiryDt.getForm().getPrefixLabelKey()+profExpiryDt.getId();
						String idLableName = resolve(id);
						listError.add(new SSMLabel(listError.newChildId() , resolve("page.lbl.pcert.preEditSecretaryPcert.alertDesc.profValidityMust30days", idLableName) ));	
					}
					
					pcertChangesForm.setPcertQualificationForm(qualificationForm);
					
					if(listError.size()==0) {
//						SecretaryPcertReg pcertReg = secretaryPcertRegService.findByIdWithData(pcertChangesForm.getPcertRegNo());
						secretaryPcertChangesFormService.reSubmit(pcertChangesForm, queryAnswer.getValue());
						getSession().setAttribute("pcertChangesForm_",null);
						
						String msj = resolve("page.lbl.pcert.changes.successResubmit", pcertChangesForm.getPcertChangesFormRefNo());
						storeSuccessMsg(msj);
						setResponsePage(new DashboardPcert());
					}
					
					if(listError.size()>0) {
						wmcError.setVisible(true);
						target.add(wmcError);
						
						this.setEnabled(false);
						pcertChangesForm.setDeclarationChkBox(false);
						declarationChkBox.setDefaultModelObject(pcertChangesForm.getDeclarationChkBox());
						target.add(this);
						target.add(declarationChkBox);
					}
				}
				
			};
			reSubmit.setEnabled(false);
			
			reSubmit.setOutputMarkupId(true);
			add(reSubmit);
			
			declarationChkBox = new SSMAjaxCheckBox("declarationChkBox", new PropertyModel(pcertChangesForm, "declarationChkBox") ) {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					if (String.valueOf(true).equals(getValue())) {
						pcertChangesForm.setDeclarationChkBox(true);
					} else {
						pcertChangesForm.setDeclarationChkBox(false);
					}
					
					if(reSubmit.isVisible()){
						reSubmit.setEnabled(pcertChangesForm.getDeclarationChkBox());
						target.add(reSubmit);
					}
					
					if(submitChanges.isVisible()){
						submitChanges.setEnabled(pcertChangesForm.getDeclarationChkBox());
						target.add(submitChanges);
					}
				}
			};
			declarationChkBox.setOutputMarkupId(true);
			add(declarationChkBox);
			
			setOutputMarkupId(true);
			
			
			
			//IN PROCESS
			if(Parameter.PCERT_FORM_STATUS_IN_PROCESS.equals(pcertChangesForm.getStatus())) {
				searchLS.setVisible(false);
				queryText.setVisible(false);
				queryAnswer.setVisible(false);
				reSubmit.setVisible(false);
				submitChanges.setVisible(false);
			}if(Parameter.PCERT_FORM_STATUS_QUERY.equals(pcertChangesForm.getStatus())) {
				if(Parameter.PCERT_PROF_BODY_CODE_LS.equals(pcertChangesForm.getPcertQualificationForm().getProfType())) {
					searchLS.setVisible(true);
				}
				queryText.setVisible(true);
				queryAnswer.setVisible(true);
				reSubmit.setVisible(true);
				submitChanges.setVisible(false);
			}else {
				queryText.setVisible(false);
				queryAnswer.setVisible(false);
				reSubmit.setVisible(false);
				submitChanges.setVisible(true);
			}
			
			generateDiscardButton(this, pcertChangesForm);

		}
		
		private void validateFileUpload(FileUploadField fileUploadField) {
			
			String id = fileUploadField.getForm().getPrefixLabelKey()+fileUploadField.getId();
			String idLableName = resolve(id);
			
			FileUpload fileUpload = fileUploadField.getFileUpload();
			if(fileUpload.getBytes().length>3145728){
//				listError.add(new SSMLabel(listError.newChildId() , resolve("page.lbl.pcert.fileExceedUploadSize", idLableName) ));				
			}else{
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(fileUpload.getBytes());
					PDDocument document = PDDocument.load(bais);
					document.close();
				} catch (Exception e) {
					listError.add(new SSMLabel(listError.newChildId() , resolve("page.lbl.pcert.fileNotInPDF", idLableName)));
				}
			}
		}
		
		private void saveIntoSession(SecretaryPcertQualificationForm pcertQualificationForm){
			//Get from session
    		if(getSession().getAttribute("pcertChangesForm_") !=  null) {
    			SecretaryPcertChangesForm pcertChangesForm = (SecretaryPcertChangesForm) getSession().getAttribute("pcertChangesForm_");
    			
    			if(pcertChangesForm != null){
    				if(pcertChangesForm.getIsQualificationChange()){
    					pcertChangesForm.setPcertQualificationForm(pcertQualificationForm);
    				}
            	}
    			
    			//Set into session
    			getSession().setAttribute("pcertChangesForm_", pcertChangesForm);
    		}
		}
		
		
		
	}
	
private class PcertFileUploadModel implements Serializable{
		
		private List<FileUpload> fileUploadMembershipCert;
		private List<FileUpload> fileUploadRenewalReciept;
		
		public List<FileUpload> getFileUploadMembershipCert() {
			return fileUploadMembershipCert;
		}
		public void setFileUploadMembershipCert(List<FileUpload> fileUploadMembershipCert) {
			this.fileUploadMembershipCert = fileUploadMembershipCert;
		}
		public List<FileUpload> getFileUploadRenewalReciept() {
			return fileUploadRenewalReciept;
		}
		public void setFileUploadRenewalReciept(List<FileUpload> fileUploadRenewalReciept) {
			this.fileUploadRenewalReciept = fileUploadRenewalReciept;
		}
	}

}