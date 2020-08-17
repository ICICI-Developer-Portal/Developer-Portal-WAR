package com.icici.apigw.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icici.apigw.common.PortalException;
import com.icici.apigw.common.SMTPCase;
import com.icici.apigw.dao.ApiDataDao;
import com.icici.apigw.dao.ApiDataDaoImpl;
import com.icici.apigw.dao.CmsUsersDao;
import com.icici.apigw.dao.CmsUsersDaoImpl;
import com.icici.apigw.db.DBConnUtil;
import com.icici.apigw.db.DbUtil;
import com.icici.apigw.model.Additional_details_model;
import com.icici.apigw.model.ApiData;
import com.icici.apigw.model.ApiModel;
import com.icici.apigw.model.ApiPacket;
import com.icici.apigw.model.ApiRawData;
import com.icici.apigw.model.Appathon_Details_Model;
import com.icici.apigw.model.ApplicationModel;
import com.icici.apigw.model.AuthTypeModel;
import com.icici.apigw.model.DocumentDetails;
import com.icici.apigw.model.ErrorCode;
import com.icici.apigw.model.LoginModel;
import com.icici.apigw.model.MenuDescriptionModel;
import com.icici.apigw.model.MenuTreeModel;
import com.icici.apigw.model.MerchantOnboardingDt;
//import com.icici.apigw.model.MerchantUser;
import com.icici.apigw.model.MyAppModel;
import com.icici.apigw.model.PlatformModel;
import com.icici.apigw.model.PortalUserRegDt;
import com.icici.apigw.model.ProfileModel;
import com.icici.apigw.model.ResponseModel;
import com.icici.apigw.model.TestCaseDetails;
import com.icici.apigw.model.TestTxDetails;
import com.icici.apigw.model.data_resp_domain_model;
import com.icici.apigw.model.data_resp_sub_api_model;
import com.icici.apigw.model.data_resp_sub_domain_model;
import com.icici.apigw.util.AESenc;
//import com.icici.apigw.util.CommonUtil;
import com.icici.apigw.util.ConfigUtil;
import com.icici.apigw.util.GwConstants;
import com.icici.apigw.util.HttpClient;
import com.icici.apigw.util.MailTemplete;
import com.icici.apigw.util.Utility;
import com.sun.jersey.server.impl.model.method.dispatch.MultipartFormDispatchProvider;

//import com.mashape.unirest.http.HttpResponse;
//import com.mashape.unirest.http.JsonNode;
//import com.mashape.unirest.http.Unirest;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path("/")
public class RestApi {
	private static final Logger LOGGER = LogManager.getLogger(RestApi.class);

	private HttpSession session;

	public static ArrayList<PlatformModel> platformModels;
	public static ArrayList<AuthTypeModel> authTypeModels;

	private CmsUsersDao cmsUsersDao = new CmsUsersDaoImpl();
	private ApiDataDao apiDataDao = new ApiDataDaoImpl();

	@javax.ws.rs.core.Context
	private HttpServletRequest sr;
	HttpServletRequest request = null;
	@Context
	private HttpServletRequest req;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {

		byte[] actualByteUname = Base64.getDecoder().decode(username);
		byte[] actualBytePwd = Base64.getDecoder().decode(password);

		username = new String(actualByteUname);
		password = new String(actualBytePwd);

		ResponseModel responseModel = new ResponseModel();
		if (username == null || username.length() <= 0) {
			responseModel.setMessage("Please enter user name.");
			return Response.ok(responseModel).build();
		}
		if (password == null || password.length() <= 0) {
			responseModel.setMessage("Please enter password.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		LoginModel loginModel = null;
		try {
			if (cmsUsersDao.isUsernameExist(username)) {
				if (cmsUsersDao.isUsernamePasswordExist(username, password)) {

					loginModel = cmsUsersDao.login(username, password);

					if (loginModel != null) {
						if (loginModel.enabled == 0) {
							responseModel.setMessage("Your account has not been activated.");
						} else {
							responseModel.setMessage("Success");
							responseModel.setStatus(true);
							loginModel.companyName = apiDataDao.getCompanyName(username);
							responseModel.setData(loginModel);
							session = req.getSession(true);
							LOGGER.info("loginModel.id:: " + username);
							session.setAttribute("SessionUserId", username);
							LOGGER.info("LoginSession:: " + session.getAttribute("SessionUserId"));

							String JWTToken = apiDataDao.getJWTToken(loginModel.username);
							boolean isNewTokenCreated = false;
							if (JWTToken == null) {
								JWTToken = createJWT(loginModel.username);
								isNewTokenCreated = true;
							}
							responseModel.setJWTToken(JWTToken);
							try {
								if (isNewTokenCreated) {
									apiDataDao.saveJWTToken(loginModel.username, JWTToken);
								}
							} catch (JSONException | IOException | SQLException e1) {
								// TODO Auto-generated catch block
								StringWriter ex = new StringWriter();
								e1.printStackTrace(new PrintWriter(ex));
								LOGGER.error(ex.toString());
							}
						}
					} else {
						responseModel.setMessage("Invalid username or password.");
					}

				} else {
					responseModel.setMessage("Invalid password.");
				}

			} else {
				responseModel.setMessage("Invalid username.");
			}

		} catch (Exception e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					StringWriter ex = new StringWriter();
					e.printStackTrace(new PrintWriter(ex));
					LOGGER.error(ex.toString());
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
//		return Response.ok(responseModel).build();
		return Response.status(200).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").entity(responseModel).build();
	}

	@POST
	@Path("/loginJWT")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginJWT(@FormParam("username") String username, @FormParam("password") String password) {
		byte[] actualByteUname = Base64.getDecoder().decode(username);
		byte[] actualBytePwd = Base64.getDecoder().decode(password);

		username = new String(actualByteUname);
		password = new String(actualBytePwd);

		String JWTToken1 = createJWT(username);
		try {
			apiDataDao.saveJWTToken(username, JWTToken1);
		} catch (JSONException | IOException | SQLException e1) {
			// TODO Auto-generated catch block
			StringWriter ex = new StringWriter();
			e1.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
		}

		ResponseModel responseModel = new ResponseModel();
		if (username == null || username.length() <= 0) {
			responseModel.setMessage("Please enter user name.");
			return Response.ok(responseModel).build();
		}
		if (password == null || password.length() <= 0) {
			responseModel.setMessage("Please enter password.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			LoginModel loginModel = cmsUsersDao.login(username, password);

			if (loginModel != null) {
				if (loginModel.enabled == 0) {
					responseModel.setMessage("Your account not activated yet.");
				} else {
					responseModel.setMessage("Success");
					responseModel.setStatus(true);
					loginModel.companyName = apiDataDao.getCompanyName(username);
					responseModel.setData(loginModel);
					session = req.getSession(true);
					LOGGER.info("loginModel.id:: " + username);
					session.setAttribute("SessionUserId", username);
					LOGGER.info("LoginSession:: " + session.getAttribute("SessionUserId"));
					String JWTToken = createJWT(username);
					responseModel.setJWTToken(JWTToken);
					try {
						apiDataDao.saveJWTToken(username, JWTToken);
					} catch (JSONException | IOException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} else {
				responseModel.setMessage("Invalid username or password.");
			}
		} catch (Exception e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	/**
	 * @param username
	 * @param password
	 * @param email
	 * @param firstname
	 * @param lastname
	 * @return
	 */
	@POST
	@Path("/sign_up")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sign_up(@FormParam("username") String username, @FormParam("password") String password,
			@FormParam("email") String email, @FormParam("firstname") String firstname,
			@FormParam("lastname") String lastname, @FormParam("domainNm") String domainname,
			@FormParam("companyName") String compnyname, @FormParam("contactNo") String mobileno,
			@FormParam("partnerCode") String partnerCode) {
		String ip = "";
		try {
			ip = sr.getRemoteAddr();
		} catch (Exception e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
		}
		ResponseModel responseModel = new ResponseModel();
		if (username == null || username.length() <= 0) {
			responseModel.setMessage("Please enter user name.");
			return Response.ok(responseModel).build();
		}
		if (password == null || password.length() <= 0) {
			responseModel.setMessage("Please enter password.");
			return Response.ok(responseModel).build();
		}
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		String domain = email.substring(email.indexOf("@") + 1, email.lastIndexOf("."));
		domain = domain.toLowerCase();
		int AutoApprove = 0;
		try {
			AutoApprove = apiDataDao.isEmailDomainApproved(domain);
		} catch (IOException | SQLException e1) {
			StringWriter ex = new StringWriter();
			e1.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
		}

		try {
			// Check UserName Existance
			boolean isExists = cmsUsersDao.isUsernameExist(username);
			if (isExists) {
				responseModel.setMessage("Username already exists.");
				return Response.ok(responseModel).build();
			}
			isExists = cmsUsersDao.isEmailExist(email);
			if (isExists) {
				responseModel.setMessage("Email Address already exists.");
			} else {
				long regNo = cmsUsersDao.registration(username, password, email, firstname, lastname, ip, mobileno,
						AutoApprove);
				if (regNo > 0) {
					responseModel.setStatus(true);
					responseModel.setMessage("Success");

					// Send Mail
					boolean isSent = false;
					if (AutoApprove == 1) {
						SMTPCase smtpCase = new SMTPCase();
						String html = MailTemplete.NewRegistration
								.replaceAll("__FULL_NAME__",
										(firstname != null ? firstname : "") + " " + (lastname != null ? lastname : ""))
								.replaceAll("__REG_NO__", String.valueOf(regNo));
						isSent = smtpCase.send(email, MailTemplete.NewRegistrationSubject, html);
					} else {

						SMTPCase smtpCase = new SMTPCase();
						String html = MailTemplete.NewRegistration_Old
								.replaceAll("__FULL_NAME__",
										(firstname != null ? firstname : "") + " " + (lastname != null ? lastname : ""))
								.replaceAll("__REG_NO__", String.valueOf(regNo));
						isSent = smtpCase.send(email, MailTemplete.NewRegistrationSubject, html);

						/*************** Mail TO API Dev Team *****************/
						String emailsToApiDevTeam = ConfigUtil.get("mail.approval.to");
						String htmlMailToAPITeam = MailTemplete.NewRegistration_ToAPITEAM
								.replaceAll("__FullName__", (firstname != null ? firstname : ""))
								.replaceAll("__Username__", (username != null ? username : ""))
								.replaceAll("__Company__", (compnyname != null ? compnyname : ""))
								.replaceAll("__Domain__", (domainname != null ? domainname : ""))
								.replaceAll("__Mobile__", (mobileno != null ? mobileno : ""))
								.replaceAll("__EMAIL__", (email != null ? email : ""))
								.replaceAll("__PartnerCode__", (partnerCode != null ? partnerCode : ""));
						isSent = smtpCase.send(emailsToApiDevTeam, MailTemplete.NewRegistration_ToAPITEAMSubject,
								htmlMailToAPITeam);

					}
					if (isSent)
						System.out.println("Mail Sent, After user registration..");
				} else {
					responseModel.setMessage("Unable to process, Please try again.");
				}
			}
		} catch (Exception e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/check_username")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response check_username(@FormParam("username") String username) {
		ResponseModel responseModel = new ResponseModel();
		if (username == null || username.length() <= 0) {
			responseModel.setMessage("Please enter username.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("select id from cmsusers WHERE username = ?");
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					break;
				}
			}
			if (isExists) {
				responseModel.setStatus(false);
				responseModel.setMessage("Already Exists");
			} else {
				responseModel.setStatus(true);
				responseModel.setMessage("Available");
			}
		} catch (SQLException e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/check_email")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response check_email(@FormParam("email") String email) {
		ResponseModel responseModel = new ResponseModel();
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("select id from cmsusers WHERE email = ?");
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					break;
				}
			}
			if (isExists) {
				responseModel.setStatus(false);
				responseModel.setMessage("Already Exists");
			} else {
				responseModel.setStatus(true);
				responseModel.setMessage("Available");
			}
		} catch (SQLException e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/forget_password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response forget_password(@FormParam("username") String username) {
		ResponseModel responseModel = new ResponseModel();
		if (username == null || username.length() <= 0) {
			responseModel.setMessage("Please enter username.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement(
					"select id, email, firstName, lastName, username from cmsusers WHERE (email = ? or username = ?)");
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, username);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			long id = 0;
			String email = "";
			String firstName = "";
			String lastName = "";
			String UserID = null;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					id = resultSet.getLong("id");
					email = resultSet.getString("email");
					firstName = resultSet.getString("firstName");
					lastName = resultSet.getString("lastName");
					UserID = resultSet.getString("username");
					break;
				}
			}
			if (isExists && id > 0) {
				String token = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
				preparedStatement.close();
				preparedStatement = connection.prepareStatement("update cmsusers set forgetToken = ? where id = ?");
				preparedStatement.setString(1, token);
				preparedStatement.setLong(2, id);
				int i = preparedStatement.executeUpdate();
				if (i > 0) {
					String url = GwConstants.APP_SERVER_URL + "#/" + "reset_password?token="
							+ Utility.encodeBase64(AESenc.encrypt(String.valueOf(id) + "/" + token));
					SMTPCase smtpCase = new SMTPCase();
					String html = MailTemplete.ResetPassword
							.replaceAll("__FULL_NAME__",
									(firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""))
							.replaceAll("__RESET_LINK__", url)
							.replaceAll("__USERNAME__", (UserID != null ? UserID : ""));
					boolean isSent = smtpCase.send(email, MailTemplete.ResetPasswordSubject, html);
					if (isSent) {
						responseModel.setStatus(true);
						responseModel.setMessage("Success");
					} else {
						responseModel.setMessage("Unable to send reset password email, Please try again.");
					}
				} else {
					responseModel.setMessage("Unable to process, Please try again.");
				}
			} else {
				responseModel.setMessage("Please enter correct username.");
			}
		} catch (Exception e) {
			StringWriter ex = new StringWriter();
			e.printStackTrace(new PrintWriter(ex));
			LOGGER.error(ex.toString());
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/reset_password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response reset_password(@FormParam("token") String token, @FormParam("password") String password) {
		ResponseModel responseModel = new ResponseModel();
		if (token == null || token.length() <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String tempData = AESenc.decrypt(Utility.decodeBase64(token));
			String data[] = tempData.split("/");
			if (data.length > 1) {
				long id = 0;
				try {
					id = Long.parseLong(data[0].trim());
				} catch (Exception e) {
					writeLogs(e);
				}
				if (id > 0 && data[1] != null && data[1].trim().length() > 0) {
					connection = DbUtil.getLrsConnection();
					preparedStatement = connection
							.prepareStatement("select id, email from cmsusers WHERE id = ? and forgetToken = ?");
					preparedStatement.setLong(1, id);
					preparedStatement.setString(2, data[1].trim());
					resultSet = preparedStatement.executeQuery();
					boolean isExists = false;
					if (resultSet.isBeforeFirst()) {
						while (resultSet.next()) {
							isExists = true;
							break;
						}
					}
					if (isExists) {
						if (password == null || password.length() <= 0) {
							responseModel.setMessage("Please enter new password.");
						} else {
							preparedStatement = connection
									.prepareStatement("update cmsusers set  hmacKey=null, password = ? where id = ?");
							preparedStatement.setString(1, Utility.sha1(password));
							preparedStatement.setLong(2, id);
							int i = preparedStatement.executeUpdate();
							if (i > 0) {
								responseModel.setStatus(true);
								responseModel.setMessage("Password changed successfully.");
								preparedStatement = connection
										.prepareStatement("update cmsusers set forgetToken = '' where id = ?");
								preparedStatement.setLong(1, id);
								preparedStatement.executeUpdate();
							} else {
								responseModel.setMessage("Unable to process, Please try again.");
							}
						}
					} else {
						responseModel.setMessage("Invalid token, Please try again.");
					}
				} else {
					responseModel.setMessage("Invalid request, Please try again.");
				}
			} else {
				responseModel.setMessage("Invalid request, Please try again.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/email_otp")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response email_otp(@FormParam("email") String email) {
		ResponseModel responseModel = new ResponseModel();
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement(
					"select COALESCE(mail_verify, 0) AS mail_verify from cmsusers WHERE LENGTH(COALESCE(email, '')) > 0 AND email = ? ");
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			boolean mail_verify = false;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					mail_verify = resultSet.getBoolean("mail_verify");
					break;
				}
			}
			if (isExists) {
				if (!mail_verify) {
					String mail_token = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
					preparedStatement.close();
					preparedStatement = connection
							.prepareStatement("update cmsusers set mail_token = ? where email = ?");
					preparedStatement.setString(1, mail_token);
					preparedStatement.setString(2, email);
					int i = preparedStatement.executeUpdate();
					if (i > 0) {
						String url = GwConstants.APP_SERVER_URL + "email_verify?token="
								+ Utility.encodeBase64(AESenc.encrypt(mail_token));
						SMTPCase smtpCase = new SMTPCase();
						StringBuilder html = new StringBuilder();
						html.append("Welcome!<br>");
						html.append("Click the following link to verify your email<br>");
						html.append("<a href=\"" + url + "\" target=\"_blank\">" + url + "</a><br>");
						html.append("Note: If you are NOT trying to sign up, Please ignore this email.");
						html.append("<br><br>");
						html.append(
								"Thank you, <br><a href=\"http://www.icicibank.com\" target=\"_blank\">www.icicibank.com</a>");
						boolean isSent = smtpCase.send(email, "Email Verification", html.toString());
						if (isSent) {
							responseModel.setStatus(true);
							responseModel.setMessage("Success");
						} else {
							responseModel.setMessage("Unable to send verification email, Please try again.");
						}
					} else {
						responseModel.setMessage("Unable to process, Please try again.");
					}
				} else {
					responseModel.setMessage("Email address is already verified.");
				}
			} else {
				responseModel.setMessage("Invalid email address.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/is_email_verified")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response is_email_verified(@FormParam("email") String email) {
		ResponseModel responseModel = new ResponseModel();
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("SELECT mail_verify FROM cmsusers where and email = ?");
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			boolean mail_verify = false;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					mail_verify = resultSet.getBoolean("mail_verify");
					break;
				}
			}
			if (isExists) {
				if (mail_verify) {
					responseModel.setStatus(true);
					responseModel.setMessage("Email is verified.");
				} else {
					responseModel.setMessage("Email is not verified.");
				}
			} else {
				responseModel.setMessage("Invalid request, Please try again.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/email_verify")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response email_verify(@FormParam("email") String email, @FormParam("token") String token) {
		ResponseModel responseModel = new ResponseModel();
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		if (token == null || token.length() <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String mail_token = Utility.decodeBase64(AESenc.decrypt(token));
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection
					.prepareStatement("SELECT mail_verify FROM cmsusers where mail_token = ? and email = ?");
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, mail_token);
			resultSet = preparedStatement.executeQuery();
			boolean isExists = false;
			boolean mail_verify = false;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					isExists = true;
					mail_verify = resultSet.getBoolean("mail_verify");
					break;
				}
			}
			if (isExists) {
				if (!mail_verify) {
					preparedStatement.close();
					preparedStatement = connection.prepareStatement(
							"update cmsusers set mail_verify = ?, mail_token = ? where mail_token = ? and email = ?");
					preparedStatement.setObject(1, true);
					preparedStatement.setString(2, mail_token);
					preparedStatement.setString(3, mail_token);
					preparedStatement.setString(4, email);
					int i = preparedStatement.executeUpdate();
					if (i > 0) {
						responseModel.setStatus(true);
						responseModel.setMessage("Email verified successfully.");
					} else {
						responseModel.setMessage("Unable to process, Please try again.");
					}
				} else {
					responseModel.setMessage("Email address is already verified.");
				}
			} else {
				responseModel.setMessage("Invalid request, Please try again.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/send_otp")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response send_otp(@FormParam("mobile_no") String mobile_no) {
		ResponseModel responseModel = new ResponseModel();
		if (mobile_no == null || mobile_no.length() <= 0) {
			responseModel.setMessage("Please enter mobile number.");
			return Response.ok(responseModel).build();
		}
		try {
			String id = UUID.randomUUID().toString();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("tsxndetails2", "NA");
			jsonObject.put("tsxndetails1", "NA");
			jsonObject.put("Amount", "0.0");
			jsonObject.put("tsxn", "NA");
			jsonObject.put("appname", "Merchant_Name");
			jsonObject.put("deliverymode", "SMS");
			jsonObject.put("deliveryaddress", mobile_no);
			jsonObject.put("tsxnid1", id);

			final String output = HttpClient.post(GwConstants.OTP_CREATE_URL, "application/json",
					jsonObject.toString());
			JSONObject object = new JSONObject(output);
			if (object.getString("respcode").equalsIgnoreCase("000")) {
				responseModel.setStatus(true);
				responseModel.setData(id);
				responseModel.setMessage("Success");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setStatus(false);
			responseModel.setMessage("Exception occurred:- " + e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/verify_otp")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response verify_otp(@FormParam("mobile_no") String mobile_no, @FormParam("otp_code") String otp_code,
			@FormParam("txn_no") String txn_no) {
		ResponseModel responseModel = new ResponseModel();
		if (mobile_no == null || mobile_no.length() <= 0) {
			responseModel.setMessage("Mobile number is missing in request.");
			return Response.ok(responseModel).build();
		}
		if (otp_code == null || otp_code.trim().length() <= 0) {
			responseModel.setMessage("Please enter OTP Code.");
			return Response.ok(responseModel).build();
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("tsxndetails2", "NA");
			jsonObject.put("tsxndetails1", "NA");
			jsonObject.put("amount", "0.0");
			jsonObject.put("tsxn", "NA");
			jsonObject.put("appname", "Merchant_Name");
			jsonObject.put("deliverymode", "SMS");
			jsonObject.put("deliveryaddress", mobile_no);
			jsonObject.put("tsxnid1", txn_no);
			jsonObject.put("OTP", otp_code);

			final String output = HttpClient.post(GwConstants.OTP_VERIFY_URL, "application/json",
					jsonObject.toString());
			try {
				JSONObject object = new JSONObject(output);
				if (object.getString("respcode").equalsIgnoreCase("000")) {
					responseModel.setStatus(true);
					responseModel.setMessage("Success");
				} else {
					responseModel.setStatus(false);
					responseModel.setMessage("Invalid OTP");
				}
			} catch (JSONException e) {
				responseModel.setStatus(false);
				responseModel.setMessage("Invalid OTP");
			}

		} catch (Exception e) {
			writeLogs(e);
			responseModel.setStatus(false);
			responseModel.setMessage("Exception occurred:- " + e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	/*
	 * @POST
	 * 
	 * @Path("/send_otp")
	 * 
	 * @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * send_otp(@FormParam("mobile_no") String mobile_no) { ResponseModel
	 * responseModel = new ResponseModel(); if (mobile_no == null ||
	 * mobile_no.length() <= 0) {
	 * responseModel.setMessage("Please enter mobile number."); return
	 * Response.ok(responseModel).build(); } Connection connection = null;
	 * PreparedStatement preparedStatement = null; ResultSet resultSet = null; try {
	 * 
	 * connection = DriverManager.getConnection(url, user_name, this.password);
	 * preparedStatement =
	 * connection.prepareStatement("select * from phone_verify where phone = ?");
	 * preparedStatement.setString(1, mobile_no); resultSet =
	 * preparedStatement.executeQuery(); boolean isExists = false; if
	 * (resultSet.isBeforeFirst()) { while (resultSet.next()) { isExists = true;
	 * break; } } Random rand = new Random(); int otp_code = rand.nextInt((999999 -
	 * 111111) + 1) + 111111; int i = 0; if (isExists) { preparedStatement =
	 * connection.prepareStatement("update phone_verify set otp = ? where phone = ?"
	 * ); preparedStatement.setString(1, String.valueOf(otp_code));
	 * preparedStatement.setString(2, mobile_no); i =
	 * preparedStatement.executeUpdate(); } else { preparedStatement = connection.
	 * prepareStatement("insert into phone_verify(phone, otp) values(?, ?)");
	 * preparedStatement.setString(1, mobile_no); preparedStatement.setString(2,
	 * String.valueOf(otp_code)); i = preparedStatement.executeUpdate(); } if (i >
	 * 0) { String message = "Your OTP is : " + String.valueOf(otp_code); String url
	 * =
	 * "http://173.45.76.227/send.aspx?username=transprevo1&pass=Prevo@123&route=trans1&senderid=BCBUCK&numbers="
	 * + mobile_no + "&message=" + message; try { URL obj = new URL(url);
	 * HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 * con.setRequestMethod("GET"); responseModel.setStatus(true);
	 * responseModel.setMessage("Success"); } catch (Exception e) { writeLogs(e);
	 * responseModel.setMessage("Unable to send OTP Code, Please try again."); } }
	 * else { responseModel.setMessage("Unable to process, Please try again."); } }
	 * catch (Exception e) { writeLogs(e); responseModel.setMessage(e.getMessage());
	 * } finally { if (resultSet != null) { try { if (!resultSet.isClosed()) {
	 * resultSet.close(); } } catch (SQLException e) { writeLogs(e); } } if
	 * (preparedStatement != null) { try { preparedStatement.close(); } catch
	 * (SQLException e) { writeLogs(e); } } if (connection != null) { try {
	 * DbUtil.close(connection); } catch (SQLException e) { writeLogs(e); } } }
	 * return Response.ok(responseModel).build(); }
	 * 
	 * @POST
	 * 
	 * @Path("/verify_otp")
	 * 
	 * @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * verify_otp(@FormParam("mobile_no") String mobile_no, @FormParam("otp_code")
	 * String otp_code) { ResponseModel responseModel = new ResponseModel(); if
	 * (mobile_no == null || mobile_no.length() <= 0) {
	 * responseModel.setMessage("Mobile number is missing in request."); return
	 * Response.ok(responseModel).build(); } if (otp_code == null ||
	 * otp_code.trim().length() <= 0) {
	 * responseModel.setMessage("Please enter OTP Code."); return
	 * Response.ok(responseModel).build(); } Connection connection = null;
	 * PreparedStatement preparedStatement = null; ResultSet resultSet = null; try {
	 * 
	 * connection = DriverManager.getConnection(url, user_name, this.password);
	 * preparedStatement =
	 * connection.prepareStatement("select otp from phone_verify where phone = ?");
	 * preparedStatement.setString(1, mobile_no); resultSet =
	 * preparedStatement.executeQuery(); String sent_otp = ""; boolean isExists =
	 * false; if (resultSet.isBeforeFirst()) { while (resultSet.next()) { isExists =
	 * true; sent_otp = resultSet.getString("otp"); break; } } if (isExists &&
	 * sent_otp != null && sent_otp.length() > 0) { if (otp_code.equals(sent_otp) ||
	 * otp_code.equals("1234") || otp_code.equals("1111")) { preparedStatement =
	 * connection.prepareStatement("update phone_verify set otp = ? where phone = ?"
	 * ); preparedStatement.setString(1, ""); preparedStatement.setString(2,
	 * mobile_no); int i = preparedStatement.executeUpdate();
	 * responseModel.setStatus(true);
	 * responseModel.setMessage("Mobile number verified successfully."); } else {
	 * responseModel.setMessage("Please enter correct otp code."); } } else {
	 * responseModel.setMessage("Please click on send otp to verify mobile number."
	 * ); } } catch (Exception e) { writeLogs(e);
	 * responseModel.setMessage(e.getMessage()); } finally { if (resultSet != null)
	 * { try { if (!resultSet.isClosed()) { resultSet.close(); } } catch
	 * (SQLException e) { writeLogs(e); } } if (preparedStatement != null) { try {
	 * preparedStatement.close(); } catch (SQLException e) { writeLogs(e); } } if
	 * (connection != null) { try { DbUtil.close(connection); } catch (SQLException
	 * e) { writeLogs(e); } } } return Response.ok(responseModel).build(); }
	 */

	@POST
	@Path("/password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response password(@FormParam("id") long id, @FormParam("old_pwd") String old_pwd,
			@FormParam("new_pwd") String new_pwd, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (old_pwd == null || old_pwd.length() <= 0) {
			responseModel.setMessage("Please enter your old password.");
			return Response.ok(responseModel).build();
		}
		if (new_pwd == null || new_pwd.trim().length() <= 0) {
			responseModel.setMessage("Please enter your new password.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			boolean isExists = cmsUsersDao.isUserEnabled(id, old_pwd);
			if (isExists) {
				connection = DbUtil.getLrsConnection();
				preparedStatement = connection
						.prepareStatement("UPDATE cmsusers SET  hmacKey=null, password = ? where id = ?");
				preparedStatement.setString(1, Utility.sha1(new_pwd));
				preparedStatement.setLong(2, id);
				int i = preparedStatement.executeUpdate();
				if (i > 0) {
					responseModel.setStatus(true);
					responseModel.setMessage("Password changed successfully.");
				} else {
					responseModel.setMessage("Unable to process, Please try again.");
				}
			} else {
				responseModel.setMessage("Please enter correct old password.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/profile_get")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response profile_get(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("select * from cmsusers where id = ?");
			preparedStatement.setLong(1, id);
			resultSet = preparedStatement.executeQuery();
			ProfileModel profileModel = null;
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					profileModel = new ProfileModel();
					profileModel.firstname = resultSet.getString("firstName");
					profileModel.lastname = resultSet.getString("lastName");
					profileModel.username = resultSet.getString("username");
					profileModel.email = resultSet.getString("email");
					profileModel.profile_photo = resultSet.getString("profile_photo");
					break;
				}
			}
			if (profileModel != null) {
				responseModel.setStatus(true);
				responseModel.setMessage("Success");
				responseModel.setData(profileModel);
			} else {
				responseModel.setMessage("Unable to process, Please try again.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/Registration_Approval")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response Registration_Approve(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("UPDATE `cmsusers` SET `enabled`='true' WHERE `id`=?");
			preparedStatement.setLong(1, id);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					responseModel.setStatus(true);
					responseModel.setMessage("Success");
					responseModel.setData("Request Approved");
				}
			}

		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/Pending_Registration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response Pending_Registration(@FormParam("Search_ByName") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("select * from cmsusers where ENABLED = 'false'");
			resultSet = preparedStatement.executeQuery();
			ArrayList<ProfileModel> ProfileModels = new ArrayList<>();

			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					ProfileModel profileModel = new ProfileModel();
					profileModel.firstname = resultSet.getString("firstName");
					profileModel.lastname = resultSet.getString("lastName");
					profileModel.username = resultSet.getString("username");
					profileModel.email = resultSet.getString("email");
					profileModel.mobileno = resultSet.getString("email");
					profileModel.profile_photo = resultSet.getString("profile_photo");
					ProfileModels.add(profileModel);
				}
			}
			if (ProfileModels != null) {
				responseModel.setStatus(true);
				responseModel.setMessage("Success");
				responseModel.setData(ProfileModels);
			} else {
				responseModel.setMessage("No records founds.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/chk_pass")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response chk_pass(@FormParam("id") long id, @FormParam("old_pwd") String old_pwd,
			@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (old_pwd == null || old_pwd.length() <= 0) {
			responseModel.setMessage("Please enter your old password.");
			return Response.ok(responseModel).build();
		}
		try {
			boolean isExists = cmsUsersDao.isUserEnabled(id, old_pwd);
			if (isExists) {
				responseModel.setStatus(true);
				responseModel.setMessage("Success");
			} else {
				responseModel.setMessage("Invalid old password.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/profile_set")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response profile_set(@FormParam("id") long id, @FormParam("firstname") String firstname,
			@FormParam("lastname") String lastname, @FormParam("profile_photo") String profile_photo,
			@FormParam("old_pwd") String old_pwd, @FormParam("new_pwd") String new_pwd) {
		ResponseModel responseModel = new ResponseModel();
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (firstname == null || firstname.length() <= 0) {
			responseModel.setMessage("Please enter first name.");
			return Response.ok(responseModel).build();
		}
		if (lastname == null || lastname.length() <= 0) {
			responseModel.setMessage("Please enter last name.");
			return Response.ok(responseModel).build();
		}
		if (old_pwd != null && old_pwd.length() > 0) {
			if (new_pwd == null || new_pwd.length() <= 0) {
				responseModel.setMessage("Please enter new password.");
				return Response.ok(responseModel).build();
			}
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			boolean isExists = true;
			if (old_pwd != null && old_pwd.length() > 0) {
				isExists = false;
				preparedStatement = connection
						.prepareStatement("select enabled from cmsusers where id = ? AND password = ?");
				preparedStatement.setLong(1, id);
				preparedStatement.setString(2, Utility.sha1(old_pwd));
				resultSet = preparedStatement.executeQuery();
				if (resultSet.isBeforeFirst()) {
					while (resultSet.next()) {
						isExists = true;
						break;
					}
				}
			}
			if (isExists) {
				preparedStatement.close();
				preparedStatement = connection
						.prepareStatement("UPDATE cmsusers SET firstName = ?, lastName = ? where id = ?");
				preparedStatement.setString(1, firstname);
				preparedStatement.setString(2, lastname);
				preparedStatement.setLong(3, id);
				int i = preparedStatement.executeUpdate();
				if (i > 0) {
					if (old_pwd != null && old_pwd.length() > 0) {
						preparedStatement = connection
								.prepareStatement("UPDATE cmsusers SET  hmacKey=null, password = ?, where id = ?");
						preparedStatement.setString(1, Utility.sha1(new_pwd));
						preparedStatement.setLong(2, id);
					}
					if (profile_photo != null && profile_photo.length() > 0) {
						preparedStatement = connection
								.prepareStatement("UPDATE cmsusers SET profile_photo = ?, where id = ?");
						preparedStatement.setString(1, profile_photo);
						preparedStatement.setLong(2, id);
					}
					responseModel.setMessage("Success");
					responseModel.setStatus(true);
				} else {
					responseModel.setMessage("Unable to process, Please try again.");
				}
			} else {
				responseModel.setMessage("Invalid old password.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/my_applications")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response my_applications(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("SELECT * FROM lrsapplicationaudit where uid = ?");
			preparedStatement.setLong(1, id);
			resultSet = preparedStatement.executeQuery();
			ArrayList<ApplicationModel> applicationModels = new ArrayList<>();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					ApplicationModel applicationModel = new ApplicationModel();
					applicationModel.id = resultSet.getLong("id");
					applicationModel.app_id = resultSet.getLong("appId");
					applicationModel.title = resultSet.getString("title");
					applicationModel.app_key = resultSet.getString("appKey");
					applicationModel.status = (resultSet.getInt("pendingApproval") == 0 ? "Pending" : "Active");
					applicationModel.platform = GetPlatform(resultSet.getString("platform"));

					applicationModels.add(applicationModel);
				}
			}
			responseModel.setStatus(true);
			responseModel.setMessage("Success");
			responseModel.setData(applicationModels);
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/get_application")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response get_application(@FormParam("id") long id, @FormParam("app_id") long app_id,
			@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();

		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}

		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement(
					"SELECT jfname.forum_name, jf.forum_foreign_key FROM jforum_forum_names jfname inner join jforum_forums jf on jfname.forum_id=jf.forum_id where lang='en'");
			resultSet = preparedStatement.executeQuery();
			ArrayList<ApiModel> apiModels = new ArrayList<>();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					ApiModel apiModel = new ApiModel();
					apiModel.name = resultSet.getString("forum_name");
					apiModel.key = resultSet.getString("forum_foreign_key");
					apiModel.selected = false;
					apiModels.add(apiModel);
				}
			}
			GetPlatform();
			MyAppModel myAppModel = new MyAppModel();
			myAppModel.platforms = platformModels;
			for (int i = 0; i < myAppModel.platforms.size(); i++) {
				myAppModel.platforms.get(i).selected = false;
			}
			myAppModel.auth_types = authTypeModels;
			myAppModel.apis = apiModels;

			preparedStatement.close();
			preparedStatement = connection.prepareStatement(
					"SELECT title, platform, description, oAuthCallbackUrl, oAuthScope, oAuthType FROM lrsapplication where appId = ?");
			preparedStatement.setLong(1, app_id);
			resultSet.close();
			resultSet = preparedStatement.executeQuery();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					myAppModel.title = resultSet.getString("title");
					myAppModel.platform = resultSet.getString("platform");
					for (int i = 0; i < myAppModel.platforms.size(); i++) {
						if (myAppModel.platforms.get(i).platform_id.equalsIgnoreCase(myAppModel.platform)) {
							myAppModel.platforms.get(i).selected = true;
							break;
						}
					}
					myAppModel.description = resultSet.getString("description");
					myAppModel.call_back_url = resultSet.getString("oAuthCallbackUrl");
					myAppModel.scope = resultSet.getString("oAuthScope");
					myAppModel.auth_type = resultSet.getString("oAuthType");
					for (int i = 0; i < myAppModel.auth_types.size(); i++) {
						if (myAppModel.auth_types.get(i).id.equalsIgnoreCase(myAppModel.auth_type)) {
							myAppModel.auth_types.get(i).selected = true;
							break;
						}
					}
					break;
				}
			}
			preparedStatement.close();
			preparedStatement = connection.prepareStatement("SELECT serviceUUID FROM lrsapikey where appId = ?");
			preparedStatement.setLong(1, app_id);
			resultSet.close();
			resultSet = preparedStatement.executeQuery();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					String serviceUUID = resultSet.getString("serviceUUID");
					for (int ai = 0; ai < myAppModel.apis.size(); ai++) {
						String keyName = myAppModel.apis.get(ai).key;
						if (serviceUUID.trim().toLowerCase().equalsIgnoreCase(keyName.trim().toLowerCase())) {
							myAppModel.apis.get(ai).selected = true;
							break;
						}
					}
				}
			}
			responseModel.setStatus(true);
			responseModel.setMessage("Success");
			responseModel.setData(myAppModel);
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/save_application")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response save_application(@FormParam("id") long id, @FormParam("app_id") long app_id,
			@FormParam("app_name") String app_name, @FormParam("description") String description,
			@FormParam("call_back_url") String call_back_url, @FormParam("platform") String platform,
			@FormParam("scope") String scope, @FormParam("auth_type") String auth_type,
			@FormParam("apis") List<String> apis, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (app_name == null || app_name.length() <= 0) {
			responseModel.setMessage("Please enter application domain.");
			return Response.ok(responseModel).build();
		}
		List<String> newApis = new ArrayList<>();
		for (int i = 0; i < apis.size(); i++) {
			String[] values = apis.get(i).split(",");
			for (int j = 0; j < values.length; j++) {
				boolean isExists = false;
				for (int k = 0; k < newApis.size(); k++) {
					if (newApis.get(k).equalsIgnoreCase(values[j])) {
						isExists = true;
						break;
					}
				}
				if (!isExists) {
					newApis.add(values[j]);
				}
			}
		}
		if (newApis == null || newApis.size() <= 0) {
			responseModel.setMessage("Please select APIs.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			long appId = 0;
			if (app_id > 0) {
				preparedStatement = connection.prepareStatement(
						"update lrsapplication set title = ?, description = ?, oAuthCallbackUrl = ?, platform = ?, oAuthScope = ?, oAuthType = ? where appId = ? ");
				preparedStatement.setString(1, app_name);
				preparedStatement.setString(2, description);
				preparedStatement.setString(3, call_back_url);
				preparedStatement.setString(4, platform);
				preparedStatement.setString(5, scope);
				preparedStatement.setString(6, auth_type);
				preparedStatement.setLong(7, app_id);
				int k = preparedStatement.executeUpdate();
				if (k > 0) {
					appId = app_id;
				}
			} else {
				preparedStatement = connection.prepareStatement(
						"insert into lrsapplication(orgId, title, description, status, oAuthCallbackUrl, platform, oAuthScope, oAuthType) "
								+ " values (78, ?, ?, 3, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, app_name);
				preparedStatement.setString(2, description);
				preparedStatement.setString(3, call_back_url);
				preparedStatement.setString(4, platform);
				preparedStatement.setString(5, scope);
				preparedStatement.setString(6, auth_type);
				preparedStatement.executeUpdate();
				resultSet = preparedStatement.getGeneratedKeys();
				if (resultSet.next()) {
					appId = resultSet.getLong(1);
				}
			}
			if (appId > 0) {
				preparedStatement = connection
						.prepareStatement("SELECT orgId FROM cmsorganizationusers  where userid = ?");
				preparedStatement.setLong(1, id);
				resultSet = preparedStatement.executeQuery();
				long orgId = 0;
				if (resultSet.isBeforeFirst()) {
					while (resultSet.next()) {
						orgId = resultSet.getLong("orgId");
						break;
					}
				}

				if (app_id <= 0) {

					String appKey = "l7xx" + UUID.randomUUID().toString().replaceAll("-", "");
					String keySecret = UUID.randomUUID().toString().replaceAll("-", "");
					preparedStatement = connection.prepareStatement(
							"UPDATE lrsapplication SET orgId = ? ,appKey = ?,keySecret=?,issuer='api.icicibank.com',status='1' WHERE appId = ?");
					preparedStatement.setLong(1, orgId);
					preparedStatement.setString(2, appKey);
					preparedStatement.setString(3, keySecret);
					preparedStatement.setLong(4, appId);
					preparedStatement.executeUpdate();

					preparedStatement = connection.prepareStatement(
							"insert into lrsapplicationaudit(appId, orgId, title, description, platform, oAuthCallbackUrl, oAuthScope, oAuthType, action, pendingApproval, uid, appKey, keySecret) "
									+ " values (?, ?, ?, ?, ?, ?, ?, ?, 'added', 1, ?, ?, ?)");
					preparedStatement.setLong(1, appId);
					preparedStatement.setLong(2, orgId);
					preparedStatement.setString(3, app_name);
					preparedStatement.setString(4, description);
					preparedStatement.setString(5, platform);
					preparedStatement.setString(6, call_back_url);
					preparedStatement.setString(7, scope);
					preparedStatement.setString(8, auth_type);
					preparedStatement.setLong(9, id);
					preparedStatement.setString(10, appKey);
					preparedStatement.setString(11, keySecret);
					preparedStatement.executeUpdate();
				} else {
					preparedStatement = connection.prepareStatement(
							"UPDATE lrsapplicationaudit SET title = ?, description = ?, platform = ?, oAuthCallbackUrl = ?, oAuthScope = ?, oAuthType = ? WHERE appId = ?");
					preparedStatement.setString(1, app_name);
					preparedStatement.setString(2, description);
					preparedStatement.setString(3, platform);
					preparedStatement.setString(4, call_back_url);
					preparedStatement.setString(5, scope);
					preparedStatement.setString(6, auth_type);
					preparedStatement.setLong(7, appId);
					preparedStatement.executeUpdate();
				}
				if (app_id > 0) {
					/*
					 * preparedStatement =
					 * connection.prepareStatement("delete from lrsapplicationaudit where appId = ?"
					 * ); preparedStatement.setLong(1, app_id); preparedStatement.executeUpdate();
					 */

					preparedStatement = connection.prepareStatement("delete from lrsapikey where appId = ?");
					preparedStatement.setLong(1, app_id);
					preparedStatement.executeUpdate();
				}

				for (int i = 0; i < newApis.size(); i++) {
					preparedStatement = connection.prepareStatement(
							"insert into lrsapikey(orgId, enabled, planId, serviceUUID, appId, status, standalone) "
									+ " values (?, 0, '1db2437af8b34992f57aedbb22fe27838f42d019', ?, ?, 1, 1)");
					preparedStatement.setLong(1, orgId);
					preparedStatement.setString(2, newApis.get(i).trim());
					preparedStatement.setLong(3, appId);
					preparedStatement.executeUpdate();
				}
				responseModel.setStatus(true);
				responseModel.setMessage("Application saved successfully.");
				return Response.ok(responseModel).build();
			} else {
				responseModel.setMessage("Unable to save application, Please try again.");
				return Response.ok(responseModel).build();
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/del_application")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response del_application(@FormParam("app_id") long app_id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		session = req.getSession(false);
		String ID = (String) session.getAttribute("SessionUserId");
		if (ID.equals(null)) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (app_id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {

			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement("delete from lrsapplication where appId = ?");
			preparedStatement.setLong(1, app_id);
			int i = preparedStatement.executeUpdate();
			if (i > 0) {
				preparedStatement = connection.prepareStatement("delete from lrsapplicationaudit where appId = ?");
				preparedStatement.setLong(1, app_id);
				preparedStatement.executeUpdate();

				preparedStatement = connection.prepareStatement("delete from lrsapikey where appId = ?");
				preparedStatement.setLong(1, app_id);
				preparedStatement.executeUpdate();

				responseModel.setStatus(true);

				responseModel.setMessage("Deleted successfully.");
			} else {
				responseModel.setMessage("Unable to delete, Please try again.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/application_services")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response application_services(@FormParam("id") long id, @FormParam("app_id") long app_id,
			@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		if (id <= 0) {
			responseModel.setMessage("Invalid request, Please try again.");
			return Response.ok(responseModel).build();
		}
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = DbUtil.getLrsConnection();
			preparedStatement = connection.prepareStatement(
					"SELECT jfname.forum_name, jf.forum_foreign_key FROM jforum_forum_names jfname inner join jforum_forums jf on jfname.forum_id=jf.forum_id where lang='en'");
			resultSet = preparedStatement.executeQuery();
			ArrayList<ApiModel> apiModels = new ArrayList<>();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					ApiModel apiModel = new ApiModel();
					apiModel.name = resultSet.getString("forum_name");
					apiModel.key = resultSet.getString("forum_foreign_key");

					apiModels.add(apiModel);
				}
			}
			preparedStatement.close();
			preparedStatement = connection.prepareStatement("SELECT serviceUUID FROM lrsapikey where appId = ?");
			preparedStatement.setLong(1, app_id);
			resultSet.close();
			resultSet = preparedStatement.executeQuery();
			if (resultSet.isBeforeFirst()) {
				while (resultSet.next()) {
					for (int i = 0; i < apiModels.size(); i++) {
						if (apiModels.get(i).key.equalsIgnoreCase(resultSet.getString("serviceUUID"))) {
							apiModels.get(i).selected = true;
						}
					}
				}
			}
			responseModel.setStatus(true);
			responseModel.setMessage("Success");
			responseModel.setData(apiModels);
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		} finally {
			if (resultSet != null) {
				try {
					if (!resultSet.isClosed()) {
						resultSet.close();
					}
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					writeLogs(e);
				}
			}
			if (connection != null) {
				DbUtil.close(connection);
			}
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/feedback")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response feedback(@FormParam("email") String email, @FormParam("location") String location,
			@FormParam("feedbackIn") String feedbackIn, @FormParam("fullName") String fullName,
			@FormParam("mobile") String mobile, @FormParam("company") String company,
			@FormParam("requirements") String requirements, @FormParam("feedback") String feedback,
			@FormParam("topic") String topic) {
		ResponseModel responseModel = new ResponseModel();
		if (email == null || email.length() <= 0) {
			responseModel.setMessage("Please enter email address.");
			return Response.ok(responseModel).build();
		}
		if (!Utility.isValidEmailAddress(email)) {
			responseModel.setMessage("Please enter correct email address.");
			return Response.ok(responseModel).build();
		}
		if (feedbackIn == null || feedbackIn.length() <= 0) {
			responseModel.setMessage("Please enter feedback.");
			return Response.ok(responseModel).build();
		}
		try {
			boolean isSaved = apiDataDao.saveFeedback(email, feedbackIn, location, fullName, mobile, company,
					requirements);

			SMTPCase smtpCase = new SMTPCase();

			String html = null;
			String subject = null;
			if (mobile == null) {
				mobile = "";
			}
			if (mobile.length() > 0) {
				subject = MailTemplete.USER_DETAILS_Subject;
				html = MailTemplete.USER_INTERESTED.replaceAll("__FULLNAME__", fullName).replaceAll("__EMAIL__", email)
						.replaceAll("__CONTACTNUMBER__", mobile).replaceAll("__LOCATION__", location)
						.replaceAll("__COMPANY__", company).replaceAll("__REQ__", requirements);
			} else {
				subject = MailTemplete.FEEDBACK_Subject;
				html = MailTemplete.FEEDBACK.replaceAll("__TOPIC__", topic).replaceAll("__EMAIL__", email)
						.replaceAll("__LOCATION__", location).replaceAll("__FEEDBACK__", String.valueOf(feedback));
			}
			String mailIds = ConfigUtil.get("mail.feedback.to");
			boolean isSent = smtpCase.send(mailIds, subject, html);
			if (isSent)
				LOGGER.info("Feedback email Sent.");

			/*************************************
			 * Mail To User
			 ******************************************/
			smtpCase = new SMTPCase();
			String htmlMailToUser = null;
			String subjectMailToUser = null;
			String serverUrl = ConfigUtil.get("server.url");
			String sendMailTo = ConfigUtil.get("mail.feedback.to");
			String mailArray[] = null;
			if (sendMailTo.contains(",")) {
				mailArray = sendMailTo.split(",");
			} else {
				mailArray = new String[1];
				mailArray[0] = sendMailTo;
			}
			sendMailTo = "";
			for (int i = 0; i < mailArray.length; i++) {
				if (sendMailTo.length() == 0) {
					sendMailTo = mailArray[i];
				} else {
					sendMailTo = sendMailTo + " or " + mailArray[i];
				}
			}
			if (mobile == null) {
				mobile = "";
			}
			if (mobile.length() > 0) {
				subjectMailToUser = MailTemplete.USER_MAIL_INTERESTED_SUBJECT;
				htmlMailToUser = MailTemplete.USER_INTERESTED_MAIL.replaceAll("__SERVERURL__", serverUrl)
						.replaceAll("__MAILID__", sendMailTo).replace("__USERNAME__", fullName);
			} else {
				subjectMailToUser = MailTemplete.USER_MAIL_FEEDBACK_SUBJECT;
				htmlMailToUser = MailTemplete.USER_FEEDBACK_MAIL.replaceAll("__SERVERURL__", serverUrl)
						.replaceAll("__MAILID__", sendMailTo).replace("__USERNAME__", "Customer");
			}

			String mailIdsToUser = email;
			boolean isSentToUser = smtpCase.send(mailIdsToUser, subjectMailToUser, htmlMailToUser);
			if (isSentToUser)
				LOGGER.info("Feedback email Sent.");

			if (isSaved) {
				responseModel.setMessage("Success");
				responseModel.setStatus(true);
				return Response.ok(responseModel).build();
			} else {
				return Response.serverError().build();
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	// public void sendMail(String email, String feedbackIn, String location, String
	// fullName, String mobile, String company, String requirements, String
	// feedback, String topic) {
	// // Send Mail
	//
	// }
	/*
	 * @POST
	 * 
	 * @Path("/load-api-data")
	 * 
	 * @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * load_api_data(@FormParam("id") long id) { try { return
	 * Response.ok(Utility.LoadApiData).build(); }catch (Exception e){ writeLogs(e);
	 * } return Response.serverError().build(); }
	 * 
	 * @POST
	 * 
	 * @Path("/load-error-codes")
	 * 
	 * @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * load_error_codes(@FormParam("id") long id) { try { return
	 * Response.ok(Utility.LoadErrorCodes).build(); }catch (Exception e){
	 * writeLogs(e); } return Response.serverError().build(); }
	 * 
	 * @POST
	 * 
	 * @Path("/load-api-packet")
	 * 
	 * @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	 * 
	 * @Produces(MediaType.APPLICATION_XHTML_XML) public Response
	 * load_api_packet(@FormParam("id") long id) { try { return
	 * Response.ok(Utility.LoadApiPacket).build(); }catch (Exception e){
	 * writeLogs(e); } return Response.serverError().build(); }
	 */

	private void GetPlatform() {
		if (platformModels == null) {
			platformModels = new ArrayList<>();
			PlatformModel platformModel;
			platformModel = new PlatformModel();
			platformModel.platform_id = "642e895c-1ca1-4929-886c-8e48b339c119";
			platformModel.platform_name = "Android";
			platformModels.add(platformModel);

			platformModel = new PlatformModel();
			platformModel.platform_id = "52dbca41-6932-4a57-9bb1-b817159b90d4";
			platformModel.platform_name = "iOS";
			platformModels.add(platformModel);

			platformModel = new PlatformModel();
			platformModel.platform_id = "506f42df-e7e6-4a60-9da4-95a78c8a1c5b";
			platformModel.platform_name = "Web Application";
			platformModels.add(platformModel);

			/*
			 * platformModel = new PlatformModel(); platformModel.platform_id =
			 * "814f01d2-e9bc-4fd0-8168-46be753aa09f"; platformModel.platform_name =
			 * "Hybrid"; platformModels.add(platformModel);
			 */
		}
		if (authTypeModels == null) {
			authTypeModels = new ArrayList<>();
			AuthTypeModel authTypeModel;

			authTypeModel = new AuthTypeModel();
			authTypeModel.id = "public";
			authTypeModel.name = "Public";
			authTypeModels.add(authTypeModel);

			authTypeModel = new AuthTypeModel();
			authTypeModel.id = "confidential";
			authTypeModel.name = "Confidential";
			authTypeModels.add(authTypeModel);
		}
	}

	private String GetPlatform(String platform) {
		GetPlatform();
		String platform_name = "";
		for (int i = 0; i < platformModels.size(); i++) {
			if (platformModels.get(i).platform_id.equals(platform)) {
				platform_name = platformModels.get(i).platform_name;
				break;
			}
		}
		return platform_name;
	}

	@POST
	@Path("/domain-and-apis")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response domain_and_apis(@FormParam("id") long id) {
		try {
			Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			List<ApiData> apiDataList = apiDataDao.getApiDetails();
			String finalApiListJson = finalJson.toJson(apiDataList);

			JSONArray jsonArray = new JSONArray(finalApiListJson);
			ArrayList<data_resp_domain_model> domain = new ArrayList<>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("ApiDomain");
				boolean isExists = false;
				for (int j = 0; j < domain.size(); j++) {
					if (domain.get(j).domain.equalsIgnoreCase(name)) {
						isExists = true;
						break;
					}
				}
				if (!isExists) {
					data_resp_domain_model model = new data_resp_domain_model();
					model.domain = name;
					model.sub_domain = new ArrayList<>();
					for (int k = 0; k < jsonArray.length(); k++) {
						JSONObject jsonObjectD = jsonArray.getJSONObject(k);
						if (jsonObjectD.getString("ApiDomain").equalsIgnoreCase(name)) {
							String sub_name = jsonObjectD.getString("ApiSubDomain");
							boolean isExistsD = false;
							for (int l = 0; l < model.sub_domain.size(); l++) {
								if (model.sub_domain.get(l).name.equalsIgnoreCase(sub_name)) {
									isExistsD = true;
									break;
								}
							}
							if (!isExistsD) {
								data_resp_sub_domain_model sub_domain_model = new data_resp_sub_domain_model();
								sub_domain_model.name = sub_name;
								sub_domain_model.api = new ArrayList<>();
								for (int m = 0; m < jsonArray.length(); m++) {
									JSONObject jsonObjectA = jsonArray.getJSONObject(m);
									if (jsonObjectA.getString("ApiDomain").equalsIgnoreCase(name)) {
										if (jsonObjectA.getString("ApiSubDomain").equalsIgnoreCase(sub_name)) {
											String api_name = jsonObjectA.getString("ApiName");
											boolean isExistsA = false;
											for (int n = 0; n < sub_domain_model.api.size(); n++) {
												if (sub_domain_model.api.get(n).name.equalsIgnoreCase(api_name)) {
													isExistsA = true;
													break;
												}
											}
											if (!isExistsA) {
												data_resp_sub_api_model api_model = new data_resp_sub_api_model();
												api_model.ApiId = jsonObjectA.getString("ApiId");
												api_model.name = api_name;
												sub_domain_model.api.add(api_model);
											}
										}
									}
								}
								model.sub_domain.add(sub_domain_model);
							}
						}
					}
					domain.add(model);
				}
			}
			return Response.ok(domain).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	// @GET
	@Path("/get-api-data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get_api_data(@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {

			List<ApiData> apiDataList = apiDataDao.getApiDetails();
			return Response.ok(finalJson.toJson(apiDataList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/get-api-data-resp")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response get_api_data_resp(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {

			List<ApiData> apiDataList = apiDataDao.getApiDetails();
			return Response.ok(finalJson.toJson(apiDataList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/load-api-data")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response load_api_data(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {

			ApiRawData apiRawData = apiDataDao.getApiDetails(id + "");
			return Response.ok(finalJson.toJson(apiRawData)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/load-error-codes")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response load_error_codes(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {

			List<ErrorCode> errorCodes = apiDataDao.getErrorCodes();
			return Response.ok(finalJson.toJson(errorCodes)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/load-api-packet")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
	public Response load_api_packet(@FormParam("id") long id, @FormParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			final ApiPacket apiPacket = apiDataDao.loadApiPacket(id + "");
			String resContentType = "text/plain";
			if (apiPacket.getRequestType() == null || apiPacket.getRequestType().isEmpty()) {
				resContentType = "text/plain";
			} else if (apiPacket.getRequestType().equalsIgnoreCase("JSON")) {
				resContentType = "application/json";
			} else if (apiPacket.getRequestType().equalsIgnoreCase("XML")) {
				resContentType = "application/xml";
			} else {
				resContentType = "text/plain";
			}
			// return Response.ok(apiPacket.getRequestPacket(), resContentType).build();
			ResponseModel rm = new ResponseModel();
			rm.setStatus(true);
			rm.setData(apiPacket.getRequestPacket());
			return Response.ok(rm).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/save-merchant-details")
	public Response saveMerchantDetails(String rBody) {

		try {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
			MerchantOnboardingDt merDt = gson.fromJson(rBody, MerchantOnboardingDt.class);
			boolean isSaved = apiDataDao.saveMerchantOnboardingData(merDt);
			if (isSaved) {
				return Response.ok("Ok").build();
			} else {
				return Response.serverError().build();
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/save-portal-details")
	public Response savePortalDetails(String rBody, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(gson.toJson(responseModel)).build();
		}
		try {

			PortalUserRegDt userReg = gson.fromJson(rBody, PortalUserRegDt.class);
			boolean isSaved = apiDataDao.saveUserRegistrationData(userReg);
			if (isSaved) {
				return Response.ok("Ok").build();
			} else {
				return Response.serverError().build();
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/save-additional-details")
	public Response saveAdditionalDetails(String rBody, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(gson.toJson(responseModel)).build();
		}
		try {

			Additional_details_model addotionalDetails = gson.fromJson(rBody, Additional_details_model.class);
			boolean isSaved = apiDataDao.saveAdditionalDetails(addotionalDetails);
			if (isSaved) {
				return Response.ok("Ok").build();
			} else {
				return Response.serverError().build();
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/fetch-jiraid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchJiraDetails(@FormParam("username") String username, @FormParam("env") String env,
			@HeaderParam("Token") String Token) {
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {
			if (env == null || env.isEmpty())
				env = "UAT";

			List<MerchantOnboardingDt> merObList = apiDataDao.fetchJiraDetails(env, username, true);
			return Response.ok(finalJson.toJson(merObList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/fetch-jiraid-v2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchJiraDetailsV2(@FormParam("username") String username, @HeaderParam("Token") String Token) {
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		try {

			List<MerchantOnboardingDt> merObList = apiDataDao.fetchJiraDetailsV2(username, false);
//			return Response.ok(finalJson.toJson(merObList)).build();

			return Response.status(200).header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").entity(finalJson.toJson(merObList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/fetch-pending-jiraid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchPendingJiraDetails(@FormParam("username") String username, @FormParam("domain") String domain,
			@HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			if ((username == null || username.isEmpty()) && (domain == null || domain.isEmpty())) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			if ((domain == null || domain.isEmpty()) && (username != null && !username.isEmpty()))
				domain = apiDataDao.getDomainByUsername(username);
			if (domain == null || domain.isEmpty()) {
				responseModel.setMessage("Error Occurred, Domain is not found by Username.");
				return Response.ok(responseModel).build();
			}
			Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			List<MerchantOnboardingDt> merObList = apiDataDao.fetchPendingJira(domain, true);
			return Response.ok(finalJson.toJson(merObList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/fetch-pending-jiraid-v2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchPendingJiraDetailsV2(@FormParam("username") String username,
			@FormParam("domain") String domain, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			if ((username == null || username.isEmpty()) && (domain == null || domain.isEmpty())) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			if ((domain == null || domain.isEmpty()) && (username != null && !username.isEmpty()))
				domain = apiDataDao.getDomainByUsername(username);
			if (domain == null || domain.isEmpty()) {
				responseModel.setMessage("Error Occurred, Domain is not found by Username.");
				return Response.ok(responseModel).build();
			}
			Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			List<MerchantOnboardingDt> merObList = apiDataDao.fetchPendingJira(domain, false);
			return Response.ok(finalJson.toJson(merObList)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/fetch-pending-userReg")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchPendingUserReg(@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			if (username == null || username.isEmpty()) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			final String domain = apiDataDao.getDomainByUsername(username);
			if (domain == null || domain.isEmpty()) {
				responseModel.setMessage("Error Occurred, Domain is not found by Username.");
				return Response.ok(responseModel).build();
			}
			List<String> pendingUsers = cmsUsersDao.getRegPendingUsers();
			Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			List<PortalUserRegDt> portalUserList = apiDataDao.fetchPendingUserReg(domain);
			// Java-8
			// List<PortalUserRegDt> portalPendingUser =
			// portalUserList.parallelStream().filter(x ->
			// pendingUsers.contains(x.getUserName())).collect(Collectors.toList());

			List<PortalUserRegDt> portalPendingUser = new ArrayList<>();
			for (PortalUserRegDt temp : portalUserList) {
				if (pendingUsers.contains(temp.getUserName())) {
					portalPendingUser.add(temp);
				}
			}
			return Response.ok(finalJson.toJson(portalPendingUser)).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/approve-pending-userReg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response approvePendingUserRegistration(@FormParam("username") String username,
			@FormParam("approverUser") String approverUser, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			if (username == null || username.isEmpty()) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			if (approverUser == null || approverUser.isEmpty()) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			boolean isApproved = cmsUsersDao.enableUser(username);
			List<String> userDetails = apiDataDao.getUserDetails(username);
			String firstname = userDetails.get(0).trim();
			String email = userDetails.get(1).trim();
			boolean isSent = false;
			if (isApproved) {
				apiDataDao.updateUserRegWithApprover(username, approverUser);
				responseModel.setMessage("Success");
				responseModel.setStatus(true);

				SMTPCase smtpCase = new SMTPCase();
				String html = MailTemplete.NewRegistration.replaceAll("__FULL_NAME__",
						(firstname != null ? firstname : ""));
				isSent = smtpCase.send(email, MailTemplete.NewRegistrationSubject, html);

			} else {
				responseModel.setMessage("Unable to approve user.");
				responseModel.setStatus(false);
			}
			return Response.ok(responseModel).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/reject-pending-userReg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response rejectPendingUserRegistration(@FormParam("username") String username,
			@FormParam("approverUser") String approverUser, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			if (username == null || username.isEmpty()) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			if (approverUser == null || approverUser.isEmpty()) {
				responseModel.setMessage("Invalid request, Please try again.");
				return Response.ok(responseModel).build();
			}
			boolean isRejected = cmsUsersDao.rejectUser(username);
			if (isRejected) {
				apiDataDao.updateUserRegWithApprover(username, approverUser);
				responseModel.setMessage("Success");
				responseModel.setStatus(true);
			} else {
				responseModel.setMessage("Unable to reject user.");
				responseModel.setStatus(false);
			}
			return Response.ok(responseModel).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/has-admin-access")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response hasAdminAccess(@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {
			boolean isAdmin = apiDataDao.hasAdminAccess(username);
			if (isAdmin) {
				responseModel.setMessage("Success");
				responseModel.setStatus(true);
			} else {
				responseModel.setMessage("UnAuthorized");
				responseModel.setStatus(false);
			}
			return Response.ok(responseModel).build();
		} catch (Exception e) {
			writeLogs(e);
		}
		return Response.serverError().build();
	}

	/* BAN213975-START */
	@POST
	@Path("/update-filepath-details")
	public Response updateFilePathDetails(String rBody, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {
		String tokenFromDB = apiDataDao.getJWTToken(username);
		String responseString = null;
		if (!tokenFromDB.equals(Token)) {
			responseString = "{\r\n" + "	\"status\":\"FAILURE\"\r\n"
					+ "	\"msg\":\"Invalid Token, Please try again.\"\r\n" + "}";
			return Response.status(200).entity(responseString).build();
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> map = mapper.readValue(rBody, Map.class);
			boolean isSaved = apiDataDao.updateFilePathDetails(map.get("filePath").toString(),
					map.get("certificate").toString(), map.get("jiraid").toString());
			// if(isSaved) {
			// return Response.ok("Ok").build();
			// }else {
			// return Response.serverError().build();
			// }

			if (isSaved) {
				responseString = "{\r\n" + "	\"status\":\"SUCCESS\"\r\n" + "}";
			} else {
				responseString = "{\r\n" + "	\"status\":\"FAILURE\"\r\n" + "	\"msg\":\"Jira ID not found.\"\r\n"
						+ "}";
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		// return Response.serverError().build();
		return Response.status(200).entity(responseString).build();
	}

	@GET
	@Path("/GetDescription")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeveloperPortalTree(@QueryParam("ID") String ID) {

		String json = null;
		try {
			List<Map<String, String>> list = null;
			try {
				list = apiDataDao.getPortalTree(ID);
			} catch (IOException e) {
				writeLogs(e);
			}
			json = new JSONObject().toString();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			json = prettyGson.toJson(list);
		} catch (SQLException | JSONException e) {
			Response.status(200).entity(e.toString());
		}

		return Response.status(200).entity(json).build();
	}

	@POST
	@Path("/sign_up_appathon")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sign_up_appathon(@FormParam("team_name") String team_name,
			@FormParam("team_captain_name") String team_captain_name,
			@FormParam("team_captain_mobile") String team_captain_mobile,
			@FormParam("team_captain_email") String team_captain_email, @FormParam("team_size") String team_size,
			@FormParam("team_members_name") String team_members_name,
			@FormParam("team_members_mobile") String team_members_mobile,
			@FormParam("team_members_email") String team_members_email, @FormParam("location") String location,
			@FormParam("company_name") String company_name, @FormParam("username") String username,
			@FormParam("password") String password, @FormParam("usertype") String usertype) {

		ResponseModel responseModel = new ResponseModel();
		try {

			boolean isSaved = apiDataDao.saveAppathonDetails(team_name, team_captain_name, team_captain_mobile,
					team_captain_email, team_size, team_members_name, team_members_mobile, team_members_email, location,
					company_name, username, password, usertype);
			if (!isSaved) {
				responseModel.setStatus(false);
				responseModel.setStatus_code(101);
				responseModel.setData("");
				responseModel.setMessage("Exception Occured, please contact administrator.");
			} else {
				// Send Mail
				boolean isSent = false;
				String emails = team_captain_email;
				SMTPCase smtpCase = new SMTPCase();
				String html = MailTemplete.AppathonMail
						.replaceAll("__TEAM_NAME__", (team_name != null ? team_name : ""))
						.replaceAll("__TEAM_CAPTAIN_NAME__", (team_captain_name != null ? team_captain_name : ""))
						.replaceAll("__TEAM_MEMBERS_NAME__", (team_members_name != null ? team_members_name : ""))
						.replaceAll("__USERNAME__", (username != null ? username : ""));
				isSent = smtpCase.send(emails, MailTemplete.AppathonMailSubject, html);

				if (isSent)
					System.out.println("Mail Sent, After user registration..");

				responseModel.setStatus(true);
				responseModel.setStatus_code(200);
				responseModel.setData("");
				responseModel.setMessage("Data Saved Succesfully.");

			}

			/****************************************************************/

			String ip = "";
			try {
				ip = sr.getRemoteAddr();
			} catch (Exception e) {
				writeLogs(e);
			}
			ResponseModel responseMode2 = new ResponseModel();
			if (username == null || username.length() <= 0) {
				responseMode2.setMessage("Please enter user name.");
				return Response.ok(responseMode2).build();
			}
			if (password == null || password.length() <= 0) {
				responseMode2.setMessage("Please enter password.");
				return Response.ok(responseMode2).build();
			}
			if (!Utility.isValidEmailAddress(team_captain_email)) {
				responseMode2.setMessage("Please enter correct email address.");
				return Response.ok(responseMode2).build();
			}
			String domain = team_captain_email.substring(team_captain_email.indexOf("@") + 1,
					team_captain_email.lastIndexOf("."));
			domain = domain.toLowerCase();
			int AutoApprove = 0;
			try {
				AutoApprove = apiDataDao.isEmailDomainApproved(domain);
			} catch (IOException | SQLException e1) {
				e1.printStackTrace();
			}

			try {
				// Check UserName Existance
				boolean isExists = cmsUsersDao.isUsernameExist(username);
				if (isExists) {
					responseMode2.setMessage("Username already exists.");
					return Response.ok(responseMode2).build();
				} else {
					long regNo = cmsUsersDao.registration(username, password, team_captain_email, team_name, team_name,
							ip, team_captain_mobile, AutoApprove);
					if (regNo > 0) {
						responseMode2.setStatus(true);
						responseMode2.setMessage("Success");

						// Send Mail
						boolean isSent = false;

						SMTPCase smtpCase = new SMTPCase();

						/*************** Mail TO API Dev Team *****************/
						String emailsToApiDevTeam = ConfigUtil.get("mail.approval.to");
						String htmlMailToAPITeam = MailTemplete.NewRegistration_ToAPITEAM
								.replaceAll("__FullName__", (team_name != null ? team_name : ""))
								.replaceAll("__Username__", (username != null ? username : ""))
								.replaceAll("__Company__", (company_name != null ? company_name : ""))
								.replaceAll("__Domain__", (team_captain_name != null ? team_captain_name : ""))
								.replaceAll("__Mobile__", (team_captain_mobile != null ? team_captain_mobile : ""))
								.replaceAll("__EMAIL__", (team_captain_email != null ? team_captain_email : ""));
						isSent = smtpCase.send(emailsToApiDevTeam, MailTemplete.NewRegistration_ToAPITEAMSubject,
								htmlMailToAPITeam);

						if (isSent)
							System.out.println("Mail Sent, After user registration..");
					} else {
						responseModel.setMessage("Unable to process, Please try again.");
					}
				}
				/****************************************************************/
			} catch (Exception e) {
				writeLogs(e);
				responseModel.setMessage(e.getMessage());
			}
			return Response.ok(responseModel).build();

		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/update_appathon_details")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update_appathon_details(@FormParam("team_name") String team_name,
			@FormParam("team_captain_name") String team_captain_name,
			@FormParam("team_captain_mobile") String team_captain_mobile,
			@FormParam("team_captain_email") String team_captain_email, @FormParam("team_size") String team_size,
			@FormParam("team_members_name") String team_members_name,
			@FormParam("team_members_mobile") String team_members_mobile,
			@FormParam("team_members_email") String team_members_email, @FormParam("location") String location,
			@FormParam("company_name") String company_name, @FormParam("username") String username,
			@FormParam("IDEA_LINK") String idea_link, @FormParam("FINAL_SUBMISSION_LINK") String final_submission_link,
			@FormParam("FINAL_URL") String final_url, @HeaderParam("Token") String Token) {

		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		try {

			boolean isSaved = apiDataDao.updateAppathonDetails(team_name, team_captain_name, team_captain_mobile,
					team_captain_email, team_size, team_members_name, team_members_mobile, team_members_email, location,
					company_name, username, idea_link, final_submission_link, final_url);
			if (isSaved == false) {
				responseModel.setStatus(false);
				responseModel.setStatus_code(101);
				responseModel.setData("");
				responseModel.setMessage("Exception Occured, please contact administrator.");
			} else {
				responseModel.setStatus(true);
				responseModel.setStatus_code(200);
				responseModel.setData("");
				responseModel.setMessage("Data Saved Succesfully.");

				boolean isSent = false;
				String emails = team_captain_email;
				SMTPCase smtpCase = new SMTPCase();
				String html = MailTemplete.UpdateAppathonMail
						.replaceAll("__TEAM_NAME__", (team_name != null ? team_name : ""))
						.replaceAll("__TEAM_CAPTAIN_NAME__", (team_captain_name != null ? team_captain_name : ""))
						.replaceAll("__TEAM_MEMBERS_NAME__", (team_members_name != null ? team_members_name : ""))
						.replaceAll("__USERNAME__", (username != null ? username : ""));
				isSent = smtpCase.send(emails, MailTemplete.UpdateAppathonMailSubject, html);

				if (isSent)
					System.out.println("Mail Sent, After user registration..");

				responseModel.setStatus(true);
				responseModel.setStatus_code(200);
				responseModel.setData("");
				responseModel.setMessage("Data Saved Succesfully.");

				/****************************************************************/

			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		}
		return Response.ok(responseModel).build();
	}

	@POST
	@Path("/fetch_appathon_details")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetch_appathon_details(@FormParam("userName") String userName, @FormParam("user") String user,
			@HeaderParam("Token") String Token) {
		LOGGER.info("Loger msg printing !!!");
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(user);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		Appathon_Details_Model model = null;
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		try {

			model = apiDataDao.fetchAppathonDetails(userName);

			if (model == null) {
				responseModel.setStatus(false);
				responseModel.setStatus_code(101);
				responseModel.setData("");
				responseModel.setMessage("Exception Occured, please contact administrator.");
			} else {
				responseModel.setStatus(true);
				responseModel.setStatus_code(200);
				responseModel.setData(model);
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		}
		return Response.ok(finalJson.toJson(responseModel)).build();
	}

	@POST
	@Path("/fetch_details_email")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetch_details_Email(@FormParam("email") String email, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {
		Gson finalJson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(finalJson.toJson(responseModel)).build();
		}
		Appathon_Details_Model model = null;

		try {

			model = apiDataDao.fetchDetailsByEmail(email);

			if (model == null) {
				responseModel.setStatus(false);
				responseModel.setStatus_code(101);
				responseModel.setData("");
				responseModel.setMessage("Exception Occured, please contact administrator.");
			} else {
				// Send Mail
				boolean isSent = false;
//				String emailsToApiDevTeam = ConfigUtil.get("mail.approval.to");
//				SMTPCase smtpCase = new SMTPCase();
//				String html = MailTemplete.AppathonMail.replaceAll("__FULL_NAME__",(firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "")).replaceAll("__REG_NO__", String.valueOf(regNo));
//				isSent = smtpCase.send(emailsToApiDevTeam, MailTemplete.AppathonMailSubject, html);

				if (isSent)
					System.out.println("Mail Sent, After user registration..");

//				responseModel.setStatus(true);
//				responseModel.setStatus_code(200);
//				responseModel.setData("");
//				responseModel.setMessage("Data Saved Succesfully.");
			}
		} catch (Exception e) {
			writeLogs(e);
			responseModel.setMessage(e.getMessage());
		}
//		return Response.ok(model.toString()).build();
		return Response.ok(finalJson.toJson(model)).build();
	}

	@POST
	@Path("/downloadFile")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadFileWithPost(InputStream incomingData, @Context HttpHeaders headers,
			@FormParam("username") String username, @HeaderParam("Token") String Token) {
		ResponseModel responseModel = new ResponseModel();
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line.trim());
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

		JsonObject model = gson.fromJson(sb.toString(), JsonObject.class);
		String fullFilepath = model.get("filePath").toString().trim().replace("\"", "");

		String path = fullFilepath;
		File fileDownload = new File(path);
		ResponseBuilder response = Response.ok((Object) fileDownload);
		String fileName = fileDownload.getName();
		response.header("Content-Disposition", "attachment;filename=" + fileName);
//        response.header("Content-Disposition", "attachment;filename=abc.pdf");
		return response.build();
	}

	@POST
	@Path("/getAdditionalParameters")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdditionalParameters(InputStream incomingData, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {

		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line.trim());
			}
		} catch (Exception e) {
			writeLogs(e);
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

		JsonObject model = gson.fromJson(sb.toString(), JsonObject.class);

		String json = null;
		try {
			Map<String, String> map = null;
			String tokenFromDB = apiDataDao.getJWTToken(username);
			if (!tokenFromDB.equals(Token)) {
				map = new HashMap<>();
				map.put("msg", "Invalid Token, Please try again.");
				json = new JSONObject().toString();
				Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
				json = prettyGson.toJson(map);
				return Response.ok(json).build();
			}
			try {
				map = apiDataDao.getAdditionalParameters(model.get("ID").toString());
			} catch (IOException e) {
				writeLogs(e);
			}
			if (map == null) {
				map = new HashMap<>();
				map.put("msg", "Invalid API_ID.");
			}
			json = new JSONObject().toString();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			json = prettyGson.toJson(map);
		} catch (SQLException | JSONException e) {
			Response.status(200).entity(e.toString());
		}

		return Response.status(200).entity(json).build();
	}

	@GET
	@Path("/getMenuTree")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMenuTree(@QueryParam("ID") String ID) {
		ArrayList<MenuTreeModel> list = null;
		MenuTreeModel model = null;
		String json = null;
		try {
			Map<String, String> map = null;
			try {
				list = apiDataDao.getMenuDetails(ID);

			} catch (IOException e) {
				writeLogs(e);
			}

			json = new JSONObject().toString();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			json = prettyGson.toJson(list);
		} catch (SQLException | JSONException e) {
			Response.status(200).entity(e.toString());
		}

		return Response.status(200).entity(json).build();
	}

	@GET
	@Path("/getMenuDescription")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMenuDescription(@QueryParam("ID") String ID) {
		ArrayList<MenuDescriptionModel> list = null;
		String json = null;
		try {
			try {
				list = apiDataDao.getMenuDescription(ID);

			} catch (IOException e) {
				writeLogs(e);
			}
			json = new JSONObject().toString();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

			Map<String, String> map = null;
			if (list == null || list.size() == 0) {
				map = new HashMap<>();
				map.put("msg", "Invalid API_ID.");
				json = prettyGson.toJson(map);
			} else {
				json = prettyGson.toJson(list);
			}

		} catch (SQLException | JSONException e) {
			Response.status(200).entity(e.toString());
		}

		return Response.status(200).entity(json).build();
	}

	@GET
	@Path("/getPortalFAQ")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPortalFAQ() {

		String json = null;
		try {
			Map<String, ArrayList<String>> map = null;
			try {
				map = apiDataDao.getPortalFAQ();
			} catch (IOException e) {
				writeLogs(e);
			}
			json = new JSONObject().toString();
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			json = prettyGson.toJson(map);
		} catch (SQLException | JSONException e) {
			Response.status(200).entity(e.toString());
		}
		return Response.status(200).entity(json).build();
	}

	/* BAN213975-END */
	/*** BAN187248-Start ***/
	@GET
	@Path("/GetCompanyDetails")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCompanyCheck(@QueryParam("Name") String Name) {
		List<String> list = null;
		try {
			list = apiDataDao.getCompanyInfo(Name);
		} catch (IOException | SQLException e) {
			Response.status(200).entity(e.toString());
		}
		return Response.status(200).entity(list).build();
	}

	/*** BAN187248-End ***/

	private static String SECRET_KEY = "Secureapi@7#";

	public static String createJWT(String username) {

		String id = "ICICI";
		String issuer = "ICICI Developer portal";
		String subject = "JWT TOken for Developer portal";
		long ttlMillis = System.currentTimeMillis();
		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(username).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
				.signWith(signatureAlgorithm, signingKey);

		// if it has been specified, let's add the expiration
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		return builder.compact();
	}

	public String getJWTToken(String Username) {
		String jwtToken = null;
		jwtToken = apiDataDao.getJWTToken(Username);
		return jwtToken;
	}

	public void writeLogs(Exception e) {
		StringWriter ex = new StringWriter();
		e.printStackTrace(new PrintWriter(ex));
		LOGGER.error(ex.toString());
	}

	// document details service

	@POST
	@Path("/getDocDetails")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDocDetails(@FormParam("docId") String docId, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {

		DocumentDetails documentDetails = null;
		Gson prettyGson = null;
		String json = null;

		ResponseModel responseModel = new ResponseModel();
		// JWT Authentication
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}

		try {

			documentDetails = apiDataDao.findDocDetails(docId);

			// System.out.println(documentDetails==null);
			if (documentDetails == null) {
				LOGGER.error(docId + ":-Document details not-Found");
				responseModel.setMessage("Document details not found");
				return Response.status(200).entity(responseModel).type(MediaType.APPLICATION_JSON).build();
			} else {
				LOGGER.info(docId + ":Document details found");

				try {
					json = new JSONObject().toString();
					prettyGson = new GsonBuilder().setPrettyPrinting().create();
				} catch (JSONException e) {
					LOGGER.error(e.toString());
					Response.status(200).entity(e.toString());
				}

				json = prettyGson.toJson(documentDetails);
			}

		} catch (Exception e) {
			writeLogs(e);
			Response.status(200).entity(e.toString());
		}

		return Response.ok().entity(json).build();
	}

	// MIS download file Service
	@POST
	@Path("/getMisFile")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getMisFile(@FormParam("fileDate") Date fileDate, @HeaderParam("userName") String userName,
			@HeaderParam("Token") String Token) {
		String filename = null;
		File fileDownload = null;
		String path = null;
		StringBuilder builder = null;
		String merchantName = null;
		ResponseModel responseModel = null;
		FileInputStream fis = null;
		boolean flag = false;
		responseModel = new ResponseModel();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");

		LOGGER.info("User:-" + userName);
		// JWT Authentication
		String tokenFromDB = apiDataDao.getJWTToken(userName);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}

		try {
			merchantName = apiDataDao.findMisFile(userName);
			LOGGER.info("MerchantName:" + merchantName);

			if (merchantName == null) {

				responseModel.setMessage("User not registered for MIS");
				responseModel.setStatus(false);
				return Response.status(200).entity(responseModel).type(MediaType.APPLICATION_JSON).build();
			} else {
				filename = merchantName + "_" + ft.format(fileDate);

				LOGGER.info("filename :=" + filename);
				builder = new StringBuilder().append(GwConstants.MIS_LOCAL).append("/").append(merchantName).append("/")
						.append(filename).append(".csv");
				path = builder.toString();
				LOGGER.info("MISLocal:" + path);

				fileDownload = new File(path);
				try {
					fis = new FileInputStream(fileDownload);
				} catch (FileNotFoundException e) {
					flag = true;
				}

				if (flag == true) {
					responseModel.setMessage("No MIS Report");
					return Response.status(200).entity(null).build();
				}
			}

		} catch (Exception e) {
			writeLogs(e);
			Response.status(200).entity(e.toString());
		}

		return Response.ok((Object) fis).header("Content-Disposition", "attachment;filename=" + filename + ".csv")
				.header("fileName", filename).build();
	}

	

	// Test API request transation entry
	@POST
	@Path("/createTxHistory")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTransactionHistory(@FormParam("headers") String headers,
			@FormParam("cType") String contentType, @FormParam("reqBody") String requestBody,
			@FormParam("resBody") String responseBody, @FormParam("apiName") String apiName,
			@FormParam("apiId") String apiId, @FormParam("testCaseId") String testCaseId,
			@FormParam("testCaseStatus") String testCaseStatus, @HeaderParam("Token") String Token,
			@HeaderParam("userName") String userName) {

		ResponseModel responseModel = new ResponseModel();
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
		// JWT Authentication
		String tokenFromDB = apiDataDao.getJWTToken(userName);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(gson.toJson(responseModel)).build();
		}

		try {
			if(headers==null) {
				headers="-";
			}
			if(contentType==null) {
				contentType="-";
			}
			if(requestBody==null) {
				requestBody="-";
			}
			if(responseBody==null) {
				responseBody="-";
			}
			if(apiName==null) {
				apiName="-";
			}
			if(testCaseId==null) {
				testCaseId="-";
			}
			if(testCaseStatus==null) {
				testCaseStatus="N";
			}
			

			boolean isSaved = apiDataDao.saveTransationHistory(userName, apiName, apiId, requestBody, responseBody,
					testCaseId, testCaseStatus);
			if (isSaved) {
				LOGGER.info("Test Api transation entry....! ");
				return Response.ok("OK").build();
			} else {
				return Response.ok("FAILD").build();
			}
		} catch (Exception e) {
			// writeLogs(e);
			responseModel.setMessage("Test Api transation entry failed");
			return Response.ok("FAILD").build();
		}

	}

	// Test API GET request transation
	@POST
	@Path("/getTxHistory")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransactionHistory(@HeaderParam("Token") String Token,
			@HeaderParam("userName") String userName) {

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = null;
		TestTxDetails testTxDetails=new TestTxDetails();
		List<TestTxDetails> listTx = new ArrayList<>();
		ResponseModel responseModel=new ResponseModel();
		// JWT Authentication
		String tokenFromDB = apiDataDao.getJWTToken(userName);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(prettyGson.toJson(responseModel)).build();
		}

		try {

			listTx = apiDataDao.getTransationHistory(userName);
			System.out.println("Trasaction MN:" + listTx);
			if (listTx == null) {
				LOGGER.error(userName + ": Transaction history not found");
				// responseModel.setMessage("Transaction history not found");
				return Response.ok().entity("NULL").build();
			} else {
				LOGGER.info(userName + ": Transaction history found");

				try {

					json = prettyGson.toJson(listTx);
				} catch (JSONException e) {
					LOGGER.error(e.toString());
					Response.status(200).entity(e.toString());
				}
			}

		} catch (Exception e) {
			writeLogs(e);
			Response.status(200).entity(e.toString());
		}

		return Response.ok().entity(json).build();
	}
	
	
	//GET Test case Information 
	@POST
	@Path("/getTestCase")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestCaseDetails(@FormParam("apiId") String apiId, @HeaderParam("username") String username,
			@HeaderParam("Token") String Token) {

		List<TestCaseDetails> list=new ArrayList<>();
		Gson prettyGson = null;
		String json = null;
		ResponseModel responseModel=new ResponseModel();

		// JWT Authentication
		String tokenFromDB = apiDataDao.getJWTToken(username);
		if (!tokenFromDB.equals(Token)) {
			responseModel.setMessage("Invalid Token, Please try again.");
			return Response.ok(responseModel).build();
		}

		try {

			list = apiDataDao.findTestDetails(apiId);

			// System.out.println(documentDetails==null);
			if (list == null) {
				System.out.println("NULL");
				LOGGER.error(apiId + ": testCase details not found");
				return Response.status(200).entity("NULL").type(MediaType.APPLICATION_JSON).build();
			} else {
				LOGGER.info(apiId + ":TestCase details details found");

				try {
					json = new JSONObject().toString();
					prettyGson = new GsonBuilder().setPrettyPrinting().create();
					json = prettyGson.toJson(list);
				} catch (JSONException e) {
					LOGGER.error(e.toString());
					Response.status(200).entity(e.toString());
				}
			}

		} catch (Exception e) {
			writeLogs(e);
			Response.status(200).entity(e.toString());
		}

		
		return Response.ok().entity(json).build();
	}
	
	
	
	
}