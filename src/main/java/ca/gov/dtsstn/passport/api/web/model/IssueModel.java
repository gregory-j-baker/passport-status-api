package ca.gov.dtsstn.passport.api.web.model;

import java.io.Serializable;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.immutables.value.Value.Style.ValidationMethod;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Greg Baker (gregory.j.baker@hrsdc-rhdcc.gc.ca)
 */
@Immutable
@Schema(name = "Issue")
@Style(validationMethod = ValidationMethod.NONE)
public interface IssueModel extends Serializable {

	@Schema(required = true)
	@JsonProperty("IssueDetails")
	String getIssueDetails();

	@Schema(required = true)
	@JsonProperty("IssueCode")
	String getIssueCode();

	@Default
	@Schema(required = true)
	@JsonProperty("IssueSeverityCode")
	default String getIssueSeverityCode() {
		return "error";
	}

	@Schema(required = false)
	@JsonProperty("IssueReferenceExpression")
	String getIssueReferenceExpression();

}
