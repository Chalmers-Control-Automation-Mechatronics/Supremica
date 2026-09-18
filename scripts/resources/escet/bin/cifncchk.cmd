@echo off

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Copyright (c) 2010, 2025 Contributors to the Eclipse Foundation
::
:: See the NOTICE file(s) distributed with this work for additional
:: information regarding copyright ownership.
::
:: This program and the accompanying materials are made available under the terms
:: of the MIT License which is available at https://opensource.org/licenses/MIT
::
:: SPDX-License-Identifier: MIT
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

%~dp0/../eclipse-escetc.exe --launcher.noRestart --launcher.suppressErrors -application org.eclipse.escet.common.app.framework.application -nosplash org.eclipse.escet.cif.eventbased org.eclipse.escet.cif.eventbased.apps.NonconflictingCheckApplication %*
