package net.loyin.controller;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.MaterialBroad;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * 流程模型控制器
 * 
 * @author lizhangyou
 */
@RouteBind(path="activititest",sys="activiti",model="activiti")
public class ModelController  extends AdminBaseController<MaterialBroad>{
 
  protected Logger logger = LoggerFactory.getLogger(getClass());
 
	public ModelController(){
		this.modelClass=MaterialBroad.class;
	}
 
/*  *//**
   * 模型列表
   *//*
  public void modelList() {
	   ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	   
	   List<Model> list = processEngine.getRepositoryService().createModelQuery().list();
	   System.err.println(list);
	   this.renderJsp("workflow/model-list");
  }*/
 
  /**
   * 创建模型
   */
  //@RequestMapping(value = "create")
  //@RequestParam("name") String name, @RequestParam("key") String key, @RequestParam("description") String description,
  //HttpServletRequest request, HttpServletResponse response
  public void create() {
    try {
    	String name = this.getAttr("name");
    	String description = this.getAttr("description");
    	String key = this.getAttr("key");
    	
    	ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
    	RepositoryService repositoryService=processEngine.getRepositoryService();
    	
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode editorNode = objectMapper.createObjectNode();
      editorNode.put("id", "canvas");
      editorNode.put("resourceId", "canvas");
      ObjectNode stencilSetNode = objectMapper.createObjectNode();
      stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
      editorNode.put("stencilset", stencilSetNode);
      Model modelData = repositoryService.newModel();
 
      ObjectNode modelObjectNode = objectMapper.createObjectNode();
      modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
      modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
      description = StringUtils.defaultString(description);
      modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
      modelData.setMetaInfo(modelObjectNode.toString());
      modelData.setName(name);
      modelData.setKey(StringUtils.defaultString(key));
 
      repositoryService.saveModel(modelData);
      repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
      HttpServletResponse response =this.getResponse();
      HttpServletRequest request=this.getRequest();
      System.err.println(request.getContextPath() + "/service/editor?id=" + modelData.getId());
      response.sendRedirect(request.getContextPath() + "/service/editor?id=" + modelData.getId());
      
    } catch (Exception e) {
      logger.error("创建模型失败：", e);
    }
  }
 
  
  
  /**
   * 根据Model部署流程
   *//*
  @RequestMapping(value = "deploy/{modelId}")
  public String deploy(@PathVariable("modelId") String modelId, RedirectAttributes redirectAttributes) {
    try {
      Model modelData = repositoryService.getModel(modelId);
      ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      byte[] bpmnBytes = null;
 
      BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
      bpmnBytes = new BpmnXMLConverter().convertToXML(model);
 
      String processName = modelData.getName() + ".bpmn20.xml";
      Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes)).deploy();
      redirectAttributes.addFlashAttribute("message", "部署成功，部署ID=" + deployment.getId());
    } catch (Exception e) {
      logger.error("根据模型部署流程失败：modelId={}", modelId, e);
    }
    return "redirect:/workflow/model/list";
  }
 
  *//**
   * 导出model的xml文件
   *//*
  @RequestMapping(value = "export/{modelId}")
  public void export(@PathVariable("modelId") String modelId, HttpServletResponse response) {
    try {
      Model modelData = repositoryService.getModel(modelId);
      BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
      JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
      BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
      byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
 
      ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
      IOUtils.copy(in, response.getOutputStream());
      String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
      response.setHeader("Content-Disposition", "attachment; filename=" + filename);
      response.flushBuffer();
    } catch (Exception e) {
      logger.error("导出model的xml文件失败：modelId={}", modelId, e);
    }
  }
 */
}