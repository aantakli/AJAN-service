/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.pluginsystem.mywelcomeplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.MessageEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;

public final class MyWelcomeUtil {

	public enum LogLevel {
		INFO,
		WARN,
		ERROR,
		FATAL,
		DEBUG,
		SUMMARY,
		NONE
	}

	private MyWelcomeUtil() {

	}

	@SuppressWarnings({"PMD.ExcessiveParameterList","PMD.ConsecutiveLiteralAppends","PMD.SystemPrintln"})
	public static void logInfo(final Object node, final UUID id, final String corr, final LogLevel lvl, final String msg) throws UnknownHostException {
		logInfo(node, id, corr, lvl, msg, InetAddress.getLocalHost().getHostAddress());
	}

	public static void logInfo(final Object node, final UUID id, final String corr, final LogLevel lvl, final String msg, final String host) {
		StringBuilder sb = new StringBuilder(2048);
		String del = " | ";
		OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
		sb.append("time=").append(now.toString()).append(del);
		sb.append("trans=").append(id.toString()).append(del);
		sb.append("corr=").append(corr).append(del);
		sb.append("lvl=").append(lvl).append(del);
		sb.append("from=").append(host).append(del);
		sb.append("srv=").append("AJAN").append(del);
		sb.append("subsrv=").append("default").append(del);
		sb.append("comp=").append(node.getClass()).append(del);
		sb.append("op=").append("class de.dfki.asr.ajan.myWelcomeApp.myWelcomeUtil").append(del);
		sb.append("msg=").append(msg);
		System.out.println(sb.toString());
	}

	public static String getCorrelationId(final AgentTaskInformation info, final BehaviorSelectQuery queryCorrId) throws URISyntaxException, MessageEvaluationException {
		Repository repo = BTUtil.getInitializedRepository(info, queryCorrId.getOriginBase());
		List<BindingSet> result = queryCorrId.getResult(repo);
		if (result.isEmpty()) {
			throw new MessageEvaluationException("No ?correlatorId defined in Message description");
		}
		BindingSet bindings = result.get(0);
		Value strValue = bindings.getValue("correlatorId");
		if (strValue == null) {
			throw new MessageEvaluationException("No ?correlatorId defined in Message description");
		} else {
			return strValue.stringValue();
		}
	}
}
